package com.gamifiedstudyhub.backend.identity.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Stores the in-flight OAuth2 authorization request in a short-lived, server-side
 * Redis cache using a random UUID token in a cookie — prevents Java Deserialization (RCE).
 */
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger log = LoggerFactory.getLogger(
            HttpCookieOAuth2AuthorizationRequestRepository.class);
    private static final String COOKIE_NAME = "oauth2_auth_request_id";
    private static final String REDIS_PREFIX = "oauth2:req:";
    private static final int MAX_AGE_SECONDS = 180;

    private final StringRedisTemplate redisTemplate;

    public HttpCookieOAuth2AuthorizationRequestRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = readCookie(request);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return null;
        }
        String redisKey = REDIS_PREFIX + cookie.getValue();
        String serialized = redisTemplate.opsForValue().get(redisKey);
        return serialized == null ? null : deserialize(serialized);
    }

    @Override
    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            expireCookie(response);
            return;
        }

        // 1. Generate a secure random ID
        String requestId = UUID.randomUUID().toString();

        // 2. Serialize and store in Redis with 180s expiration
        String serialized = serialize(authorizationRequest);
        redisTemplate.opsForValue().set(
                REDIS_PREFIX + requestId,
                serialized,
                Duration.ofSeconds(MAX_AGE_SECONDS)
        );

        // 3. Store only the random ID in a secure cookie
        Cookie cookie = new Cookie(COOKIE_NAME, requestId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(MAX_AGE_SECONDS);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            Cookie cookie = readCookie(request);
            if (cookie != null && cookie.getValue() != null) {
                redisTemplate.delete(REDIS_PREFIX + cookie.getValue());
            }
            expireCookie(response);
        }
        return authorizationRequest;
    }

    private Cookie readCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    private void expireCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private static String serialize(OAuth2AuthorizationRequest object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bos.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize OAuth2 authorization request", e);
        }
    }

    private static OAuth2AuthorizationRequest deserialize(String value) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getUrlDecoder().decode(value));
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (OAuth2AuthorizationRequest) ois.readObject();
        } catch (Exception e) {
            log.warn("Failed to deserialize OAuth2 authorization request from Redis: {}", e.getMessage());
            return null;
        }
    }
}
