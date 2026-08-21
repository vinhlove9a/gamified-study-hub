package com.gamifiedstudyhub.backend.auth.ratelimit;

import com.gamifiedstudyhub.backend.common.config.SecurityPolicyProperties;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed throttling for the unauthenticated endpoints that trigger an
 * outbound email (forgot-password, resend-verification). Without it a caller
 * can hammer these endpoints to bomb a victim's inbox and burn the SMTP
 * quota / sender reputation.
 *
 * <p>Guards three dimensions, checked BEFORE any user lookup so limits apply
 * whether or not the address exists (no user enumeration, no wasted work):
 * <ul>
 *   <li>{@code rl:email:cd:{email}}   — per-address cooldown; presence blocks</li>
 *   <li>{@code rl:email:ip:{ip}}      — per-IP counter (60s window)</li>
 *   <li>{@code rl:email:acct:{email}} — per-address counter (1h window)</li>
 * </ul>
 *
 * <p>Fails OPEN like {@link LoginRateLimiter}: if Redis is unreachable the
 * request is allowed rather than blocking password recovery on a cache outage.
 */
@Component
public class EmailRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(EmailRateLimiter.class);

    private final StringRedisTemplate redis;
    private final SecurityPolicyProperties policy;

    public EmailRateLimiter(StringRedisTemplate redis, SecurityPolicyProperties policy) {
        this.redis = redis;
        this.policy = policy;
    }

    /**
     * Call BEFORE doing any work. Throws {@link RateLimitExceededException}
     * (HTTP 429) if the caller/address is currently throttled.
     */
    public void assertAllowed(String ip, String emailLower) {
        SecurityPolicyProperties.EmailRateLimit cfg = policy.getEmailRatelimit();
        try {
            // 1. Per-address cooldown — the cheapest check; protects a specific victim.
            Long cooldownTtl = redis.getExpire(cooldownKey(emailLower), TimeUnit.SECONDS);
            if (cooldownTtl != null && cooldownTtl > 0) {
                throw new RateLimitExceededException(
                        ErrorCodes.AUTH_RATE_LIMITED,
                        "Please wait " + cooldownTtl + "s before requesting another email.");
            }

            // 2. Per-IP window — stops one source fanning out across many addresses.
            String ipKey = "rl:email:ip:" + ip;
            Long ipCount = redis.opsForValue().increment(ipKey);
            if (ipCount != null && ipCount == 1L) {
                redis.expire(ipKey, Duration.ofMinutes(1));
            }
            if (ipCount != null && ipCount > cfg.getIpPerMinute()) {
                throw new RateLimitExceededException(
                        ErrorCodes.AUTH_RATE_LIMITED,
                        "Too many email requests from your network. Please slow down.");
            }

            // 3. Per-address hourly cap — bounds total mail to any one inbox.
            String acctKey = "rl:email:acct:" + emailLower;
            Long acctCount = redis.opsForValue().increment(acctKey);
            if (acctCount != null && acctCount == 1L) {
                redis.expire(acctKey, Duration.ofHours(1));
            }
            if (acctCount != null && acctCount > cfg.getPerEmailPerHour()) {
                throw new RateLimitExceededException(
                        ErrorCodes.AUTH_RATE_LIMITED,
                        "Too many email requests for this address. Please try again later.");
            }

            // 4. Allowed → arm the cooldown for the next request.
            redis.opsForValue().set(
                    cooldownKey(emailLower), "1", Duration.ofSeconds(cfg.getEmailCooldownSeconds()));
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("Email rate limiter unavailable, failing open: {}", e.getMessage());
        }
    }

    private String cooldownKey(String emailLower) {
        return "rl:email:cd:" + emailLower;
    }
}
