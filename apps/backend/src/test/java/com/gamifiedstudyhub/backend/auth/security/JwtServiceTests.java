package com.gamifiedstudyhub.backend.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTests {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-key-at-least-32-characters-long");
        properties.setAccessTokenExpirationMinutes(60);

        jwtService = new JwtService(properties);
    }

    @Test
    void generateAndValidateAccessToken_shouldSucceed() {
        UUID userId = UUID.fromString("4a5310d2-2dcf-4f28-9620-a8f7f8750a7f");

        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);

        String token = jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtService.isAccessTokenValid(token));
        assertFalse(jwtService.isTokenExpired(token));
        assertNotNull(jwtService.extractExpiration(token));
        assertEquals(userId, jwtService.extractUserId(token));
    }

    @Test
    void invalidToken_shouldBeRejected() {
        String invalidToken = "invalid.jwt.token";

        assertFalse(jwtService.isAccessTokenValid(invalidToken));
        assertThrows(Exception.class, () -> jwtService.extractUserId(invalidToken));
    }
}
