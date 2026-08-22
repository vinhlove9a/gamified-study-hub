package com.gamifiedstudyhub.backend.platform.idempotency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;

/**
 * Makes unsafe requests idempotent: a client that retries a {@code POST/PUT/PATCH/DELETE}
 * carrying the same {@code Idempotency-Key} header gets the original response replayed
 * instead of the side effect running twice — essential for coin spends / payments.
 *
 * <p>The first response (status &lt; 500, under a size cap) is cached in Redis for
 * {@code app.idempotency.ttl-seconds}. A short-lived lock rejects a concurrent duplicate
 * that arrives before the first one finishes with {@code 409 Conflict}. OFF by default
 * ({@code app.idempotency.enabled=false}). Keyed per authenticated user so keys can't
 * collide or leak across accounts.
 */
@Component
@ConditionalOnProperty(prefix = "app.idempotency", name = "enabled", havingValue = "true")
public class IdempotencyKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "Idempotency-Key";
    private static final Set<String> METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final long MAX_CACHEABLE_BYTES = 1_048_576; // 1 MiB
    private static final Duration LOCK_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;
    private final long ttlSeconds;

    public IdempotencyKeyFilter(StringRedisTemplate redis,
                                @Value("${app.idempotency.ttl-seconds:86400}") long ttlSeconds) {
        this.redis = redis;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String idemKey = request.getHeader(HEADER);
        if (idemKey == null || idemKey.isBlank() || !METHODS.contains(request.getMethod().toUpperCase())) {
            chain.doFilter(request, response);
            return;
        }

        String base = "idem:" + principalId() + ":" + request.getMethod() + ":"
                + request.getRequestURI() + ":" + idemKey;
        String storeKey = base;
        String lockKey = base + ":lock";

        Map<Object, Object> cached = redis.opsForHash().entries(storeKey);
        if (!cached.isEmpty()) {
            replay(response, cached);
            return;
        }

        Boolean acquired = redis.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            // A first request with this key is still in flight.
            response.sendError(HttpServletResponse.SC_CONFLICT,
                    "A request with this Idempotency-Key is already being processed");
            return;
        }

        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapper);
            store(storeKey, wrapper);
        } finally {
            wrapper.copyBodyToResponse();
            redis.delete(lockKey);
        }
    }

    private void store(String storeKey, ContentCachingResponseWrapper wrapper) {
        int status = wrapper.getStatus();
        byte[] body = wrapper.getContentAsByteArray();
        if (status >= 500 || body.length > MAX_CACHEABLE_BYTES) {
            // Don't memoize server errors or oversized bodies — let the client retry for real.
            return;
        }
        String contentType = wrapper.getContentType() == null ? "" : wrapper.getContentType();
        redis.opsForHash().putAll(storeKey, Map.of(
                "status", Integer.toString(status),
                "contentType", contentType,
                "body", Base64.getEncoder().encodeToString(body)
        ));
        redis.expire(storeKey, Duration.ofSeconds(ttlSeconds));
    }

    private void replay(HttpServletResponse response, Map<Object, Object> cached) throws IOException {
        response.setStatus(Integer.parseInt((String) cached.get("status")));
        String contentType = (String) cached.get("contentType");
        if (contentType != null && !contentType.isBlank()) {
            response.setContentType(contentType);
        }
        response.setHeader("Idempotency-Replayed", "true");
        byte[] body = Base64.getDecoder().decode((String) cached.getOrDefault("body", ""));
        response.getOutputStream().write(body);
        response.flushBuffer();
    }

    private String principalId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUserId().toString();
        }
        return "anon";
    }
}
