package com.gamifiedstudyhub.backend.mfa;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Short-lived, single-use MFA login challenge stored in Redis. */
@Service
public class MfaChallengeService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final String PREFIX = "mfa:chal:";

    private final StringRedisTemplate redis;

    public MfaChallengeService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String issue(UUID userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(PREFIX + token, userId.toString(), TTL);
        return token;
    }

    /** Returns the userId and consumes the challenge, or null if invalid/expired. */
    public UUID consume(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String key = PREFIX + token;
        String userId = redis.opsForValue().get(key);
        if (userId == null) {
            return null;
        }
        redis.delete(key);
        return UUID.fromString(userId);
    }
}
