package com.gamifiedstudyhub.backend.auth.ratelimit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
class LoginRateLimiterTests {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOps;

    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginRateLimiter(redis, new SecurityPolicyProperties());
    }

    @Test
    void assertAllowed_throwsWhenAccountLocked() {
        when(redis.getExpire("lock:acct:user@example.com", TimeUnit.SECONDS)).thenReturn(120L);

        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> limiter.assertAllowed("1.2.3.4", "user@example.com"));
        assertEquals(ErrorCodes.AUTH_ACCOUNT_LOCKED, ex.getCode());
    }

    @Test
    void assertAllowed_throwsWhenIpRateExceeded() {
        when(redis.getExpire(any(), eq(TimeUnit.SECONDS))).thenReturn(-2L);
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rl:login:ip:1.2.3.4")).thenReturn(31L); // default ipPerMinute = 30

        RateLimitExceededException ex = assertThrows(
                RateLimitExceededException.class,
                () -> limiter.assertAllowed("1.2.3.4", "user@example.com"));
        assertEquals(ErrorCodes.AUTH_RATE_LIMITED, ex.getCode());
    }

    @Test
    void recordFailure_locksAccountAtThreshold() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("fail:acct:user@example.com")).thenReturn(5L); // default maxAttempts = 5
        when(valueOps.increment("locktier:acct:user@example.com")).thenReturn(1L);

        limiter.recordFailure("user@example.com");

        // Lock key is set with an exponential-backoff TTL.
        verify(valueOps).set(eq("lock:acct:user@example.com"), eq("1"), any(Duration.class));
        verify(redis).delete("fail:acct:user@example.com");
    }

    @Test
    void assertAllowed_failsOpenWhenRedisUnavailable() {
        when(redis.getExpire(any(), eq(TimeUnit.SECONDS)))
                .thenThrow(new RuntimeException("redis down"));

        assertDoesNotThrow(() -> limiter.assertAllowed("1.2.3.4", "user@example.com"));
    }

    @Test
    void recordSuccess_clearsState() {
        lenient().when(redis.delete(any(String.class))).thenReturn(true);

        limiter.recordSuccess("user@example.com");

        verify(redis).delete("fail:acct:user@example.com");
        verify(redis).delete("lock:acct:user@example.com");
        verify(redis).delete("locktier:acct:user@example.com");
    }
}
