package com.gamifiedstudyhub.backend.auth.ratelimit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.common.config.SecurityPolicyProperties;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class EmailRateLimiterTests {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    private EmailRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new EmailRateLimiter(redis, new SecurityPolicyProperties());
    }

    @Test
    void assertAllowed_armsCooldownWhenUnderAllLimits() {
        when(redis.getExpire("rl:email:cd:user@example.com", TimeUnit.SECONDS)).thenReturn(-2L);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rl:email:ip:1.2.3.4")).thenReturn(1L);
        when(valueOps.increment("rl:email:acct:user@example.com")).thenReturn(1L);

        assertDoesNotThrow(() -> limiter.assertAllowed("1.2.3.4", "user@example.com"));

        // Cooldown key is armed for the next request (default 60s).
        verify(valueOps).set(eq("rl:email:cd:user@example.com"), eq("1"), any(Duration.class));
    }

    @Test
    void assertAllowed_throwsWhenCooldownActive() {
        when(redis.getExpire("rl:email:cd:user@example.com", TimeUnit.SECONDS)).thenReturn(42L);

        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> limiter.assertAllowed("1.2.3.4", "user@example.com"));
        assertEquals(ErrorCodes.AUTH_RATE_LIMITED, ex.getCode());
    }

    @Test
    void assertAllowed_throwsWhenIpRateExceeded() {
        when(redis.getExpire(any(), eq(TimeUnit.SECONDS))).thenReturn(-2L);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rl:email:ip:1.2.3.4")).thenReturn(6L); // default ipPerMinute = 5

        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> limiter.assertAllowed("1.2.3.4", "user@example.com"));
        assertEquals(ErrorCodes.AUTH_RATE_LIMITED, ex.getCode());
    }

    @Test
    void assertAllowed_throwsWhenPerEmailCapExceeded() {
        when(redis.getExpire(any(), eq(TimeUnit.SECONDS))).thenReturn(-2L);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rl:email:ip:1.2.3.4")).thenReturn(1L);
        when(valueOps.increment("rl:email:acct:user@example.com")).thenReturn(4L); // default perEmailPerHour = 3

        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> limiter.assertAllowed("1.2.3.4", "user@example.com"));
        assertEquals(ErrorCodes.AUTH_RATE_LIMITED, ex.getCode());
    }

    @Test
    void assertAllowed_failsOpenWhenRedisUnavailable() {
        when(redis.getExpire(any(), eq(TimeUnit.SECONDS)))
                .thenThrow(new RuntimeException("redis down"));

        assertDoesNotThrow(() -> limiter.assertAllowed("1.2.3.4", "user@example.com"));
    }
}
