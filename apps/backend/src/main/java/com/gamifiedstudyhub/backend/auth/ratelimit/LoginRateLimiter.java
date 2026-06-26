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
 * Redis-backed login throttling + progressive account lockout.
 *
 * <p>Fails OPEN: if Redis is unreachable, logins are allowed rather than
 * blocking all authentication on a cache outage. Keys:
 * <ul>
 *   <li>{@code rl:login:ip:{ip}}   — per-IP attempt counter (60s window)</li>
 *   <li>{@code fail:acct:{email}}  — consecutive failure counter (window)</li>
 *   <li>{@code lock:acct:{email}}  — presence = locked, TTL = remaining lock</li>
 *   <li>{@code locktier:acct:{email}} — backoff tier for exponential lock growth</li>
 * </ul>
 */
@Component
public class LoginRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);

    private final StringRedisTemplate redis;
    private final SecurityPolicyProperties policy;

    public LoginRateLimiter(StringRedisTemplate redis, SecurityPolicyProperties policy) {
        this.redis = redis;
        this.policy = policy;
    }

    /** Call BEFORE checking the password. Throws if locked or rate-limited. */
    public void assertAllowed(String ip, String emailLower) {
        try {
            Long lockTtl = redis.getExpire(lockKey(emailLower), TimeUnit.SECONDS);
            if (lockTtl != null && lockTtl > 0) {
                throw new RateLimitExceededException(
                        ErrorCodes.AUTH_ACCOUNT_LOCKED,
                        "Account temporarily locked due to repeated failed logins. Try again in "
                                + lockTtl + "s.");
            }

            String ipKey = "rl:login:ip:" + ip;
            Long ipCount = redis.opsForValue().increment(ipKey);
            if (ipCount != null && ipCount == 1L) {
                redis.expire(ipKey, Duration.ofMinutes(1));
            }
            if (ipCount != null && ipCount > policy.getRatelimit().getIpPerMinute()) {
                throw new RateLimitExceededException(
                        ErrorCodes.AUTH_RATE_LIMITED,
                        "Too many login attempts from your network. Please slow down.");
            }
        } catch (RateLimitExceededException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("Rate limiter unavailable, failing open: {}", e.getMessage());
        }
    }

    /** Call on a failed login. Increments counters and locks the account past the threshold. */
    public void recordFailure(String emailLower) {
        try {
            String failKey = "fail:acct:" + emailLower;
            Long count = redis.opsForValue().increment(failKey);
            if (count != null && count == 1L) {
                redis.expire(failKey, Duration.ofSeconds(policy.getRatelimit().getAccountWindowSeconds()));
            }
            if (count != null && count >= policy.getLockout().getMaxAttempts()) {
                lockAccount(emailLower);
                redis.delete(failKey);
            }
        } catch (RuntimeException e) {
            log.warn("Rate limiter recordFailure failed (ignored): {}", e.getMessage());
        }
    }

    /** Call on a successful login. Clears failure/lock state. */
    public void recordSuccess(String emailLower) {
        try {
            redis.delete("fail:acct:" + emailLower);
            redis.delete("lock:acct:" + emailLower);
            redis.delete("locktier:acct:" + emailLower);
        } catch (RuntimeException e) {
            log.warn("Rate limiter recordSuccess failed (ignored): {}", e.getMessage());
        }
    }

    /** True if the account is currently locked (used for auditing). */
    public boolean isLocked(String emailLower) {
        try {
            Long ttl = redis.getExpire(lockKey(emailLower), TimeUnit.SECONDS);
            return ttl != null && ttl > 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private void lockAccount(String emailLower) {
        String tierKey = "locktier:acct:" + emailLower;
        Long tier = redis.opsForValue().increment(tierKey);
        if (tier != null && tier == 1L) {
            redis.expire(tierKey, Duration.ofDays(1));
        }
        int effectiveTier = (int) Math.min(tier == null ? 1 : tier, policy.getLockout().getMaxTier());
        long lockSeconds = policy.getLockout().getBaseLockSeconds() * (1L << (effectiveTier - 1));
        redis.opsForValue().set(lockKey(emailLower), "1", Duration.ofSeconds(lockSeconds));
        log.info("Account {} locked for {}s (tier {})", emailLower, lockSeconds, effectiveTier);
    }

    private String lockKey(String emailLower) {
        return "lock:acct:" + emailLower;
    }
}
