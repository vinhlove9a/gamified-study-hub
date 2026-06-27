package com.gamifiedstudyhub.backend.auth.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.auth.security.JwtProperties;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTests {

    @Mock
    private StringRedisTemplate redis;
    @Mock
    private ValueOperations<String, String> valueOps;
    @Mock
    private SetOperations<String, String> setOps;

    private final Map<String, String> store = new HashMap<>();
    private final Map<String, Set<String>> sets = new HashMap<>();

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setRefreshTokenExpirationDays(30);

        lenient().when(redis.opsForValue()).thenReturn(valueOps);
        lenient().when(redis.opsForSet()).thenReturn(setOps);
        lenient().when(valueOps.get(any())).thenAnswer(i -> store.get(i.getArgument(0)));
        lenient().doAnswer(i -> { store.put(i.getArgument(0), i.getArgument(1)); return null; })
                .when(valueOps).set(any(), any(), any(Duration.class));
        lenient().when(setOps.add(any(), any())).thenAnswer(i -> {
            sets.computeIfAbsent(i.getArgument(0), k -> new HashSet<>()).add(i.getArgument(1));
            return 1L;
        });
        lenient().when(setOps.members(any())).thenAnswer(i -> sets.getOrDefault(i.getArgument(0), Set.of()));
        lenient().when(redis.expire(any(), any(Duration.class))).thenReturn(true);
        lenient().when(redis.getExpire(any(), eq(TimeUnit.SECONDS))).thenReturn(100L);
        lenient().when(redis.delete(any(String.class))).thenAnswer(i -> {
            String k = i.getArgument(0);
            sets.remove(k);
            return store.remove(k) != null;
        });

        service = new RefreshTokenService(redis, props);
    }

    @Test
    void rotate_happyPath_returnsNewTokenSameUser() {
        UUID userId = UUID.randomUUID();
        String cookie = service.issue(userId);

        RefreshTokenService.Rotation rotation = service.rotate(cookie);

        assertEquals(userId, rotation.userId());
        assertNotEquals(cookie, rotation.cookieValue());
        // The rotated token works.
        assertEquals(userId, service.rotate(rotation.cookieValue()).userId());
    }

    @Test
    void rotate_reusedToken_detectsTheftAndRevokesFamily() {
        UUID userId = UUID.randomUUID();
        String cookie = service.issue(userId);
        RefreshTokenService.Rotation rotation = service.rotate(cookie);

        // Replaying the original (now-rotated) token = theft.
        RefreshTokenException ex = assertThrows(RefreshTokenException.class, () -> service.rotate(cookie));
        assertTrue(ex.isReuseDetected());

        // The whole family is burned: the freshly rotated token no longer works.
        RefreshTokenException ex2 = assertThrows(
                RefreshTokenException.class, () -> service.rotate(rotation.cookieValue()));
        assertFalse(ex2.isReuseDetected());
    }

    @Test
    void rotate_unknownToken_throwsInvalid() {
        RefreshTokenException ex = assertThrows(
                RefreshTokenException.class, () -> service.rotate("deadbeef.bogus"));
        assertFalse(ex.isReuseDetected());
    }

    @Test
    void revoke_removesToken() {
        UUID userId = UUID.randomUUID();
        String cookie = service.issue(userId);

        service.revoke(cookie);

        assertThrows(RefreshTokenException.class, () -> service.rotate(cookie));
    }
}
