package com.gamifiedstudyhub.backend.auth.token;

import com.gamifiedstudyhub.backend.auth.security.JwtProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Opaque, rotating refresh tokens stored in Redis with reuse detection.
 *
 * <p>Cookie value = {@code tokenId.secret}. Redis holds only a SHA-256 hash of
 * the secret. Each token belongs to a "family"; rotating a token tombstones the
 * old one. Presenting a tombstoned (already-rotated) token = theft → the whole
 * family is revoked, forcing re-login.
 */
@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String STATE_ACTIVE = "active";
    private static final String STATE_ROTATED = "rotated";
    private static final Duration ROTATED_GRACE = Duration.ofSeconds(120);

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RefreshTokenService(StringRedisTemplate redis, JwtProperties jwtProperties) {
        this.redis = redis;
        this.ttl = Duration.ofDays(jwtProperties.getRefreshTokenExpirationDays());
    }

    public Duration getTtl() {
        return ttl;
    }

    /** Issue a brand-new token in a fresh family (login/register/oauth/mfa). */
    public String issue(UUID userId) {
        return mint(userId, UUID.randomUUID().toString());
    }

    public record Rotation(UUID userId, String cookieValue) {
    }

    /** Validate + rotate. Throws {@link RefreshTokenException} (with reuse flag) on any problem. */
    public Rotation rotate(String cookieValue) {
        Parsed parsed = parse(cookieValue);
        String key = key(parsed.tokenId());
        String record = redis.opsForValue().get(key);
        if (record == null) {
            throw new RefreshTokenException("Refresh token is invalid or expired", false);
        }

        String[] parts = record.split("\\|", 4);
        if (parts.length != 4) {
            redis.delete(key);
            throw new RefreshTokenException("Refresh token is corrupt", false);
        }
        String secretHash = parts[0];
        UUID userId = UUID.fromString(parts[1]);
        String familyId = parts[2];
        String state = parts[3];

        if (!constantTimeEquals(secretHash, hash(parsed.secret()))) {
            throw new RefreshTokenException("Refresh token is invalid", false);
        }
        if (STATE_ROTATED.equals(state)) {
            // Replay of an already-rotated token → token theft. Burn the family.
            revokeFamily(familyId);
            throw new RefreshTokenException("Refresh token reuse detected", true);
        }

        // Tombstone the current token for a short grace window, then mint the next.
        redis.opsForValue().set(key, join(secretHash, userId, familyId, STATE_ROTATED), ROTATED_GRACE);
        String next = mint(userId, familyId);
        return new Rotation(userId, next);
    }

    /** Revoke a single token (logout). Silent if already gone. */
    public void revoke(String cookieValue) {
        try {
            Parsed parsed = parse(cookieValue);
            redis.delete(key(parsed.tokenId()));
        } catch (RefreshTokenException ignored) {
            // malformed cookie on logout — nothing to revoke
        }
    }

    public void revokeFamily(String familyId) {
        Set<String> members = redis.opsForSet().members(familyKey(familyId));
        if (members != null) {
            members.forEach(tokenId -> redis.delete(key(tokenId)));
        }
        redis.delete(familyKey(familyId));
    }

    private String mint(UUID userId, String familyId) {
        String tokenId = UUID.randomUUID().toString();
        String secret = randomSecret();
        redis.opsForValue().set(key(tokenId), join(hash(secret), userId, familyId, STATE_ACTIVE), ttl);
        redis.opsForSet().add(familyKey(familyId), tokenId);
        redis.expire(familyKey(familyId), ttl);
        return tokenId + "." + secret;
    }

    private Parsed parse(String cookieValue) {
        if (cookieValue == null || cookieValue.isBlank()) {
            throw new RefreshTokenException("Missing refresh token", false);
        }
        int dot = cookieValue.indexOf('.');
        if (dot <= 0 || dot == cookieValue.length() - 1) {
            throw new RefreshTokenException("Malformed refresh token", false);
        }
        return new Parsed(cookieValue.substring(0, dot), cookieValue.substring(dot + 1));
    }

    private record Parsed(String tokenId, String secret) {
    }

    private static String join(String hash, UUID userId, String familyId, String state) {
        return hash + "|" + userId + "|" + familyId + "|" + state;
    }

    private static String key(String tokenId) {
        return "rt:" + tokenId;
    }

    private static String familyKey(String familyId) {
        return "rtfam:" + familyId;
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
