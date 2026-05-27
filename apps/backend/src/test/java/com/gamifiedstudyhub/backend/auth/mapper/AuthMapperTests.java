package com.gamifiedstudyhub.backend.auth.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthMapperTests {

    private final AuthMapper authMapper = new AuthMapper();

    @Test
    void toUserSummary_shouldMapSafeFields() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.fromString("6adf5f43-5f4a-4ebf-b305-e32a2b1cb54e"));
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-05-27T09:00:00Z"));
        ReflectionTestUtils.setField(user, "updatedAt", Instant.parse("2026-05-27T09:30:00Z"));
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPasswordHash("secret-hash");

        UserSummaryResponse summary = authMapper.toUserSummary(user);

        assertEquals(user.getId(), summary.id());
        assertEquals("Test User", summary.fullName());
        assertEquals("test@example.com", summary.email());
        assertEquals("ACTIVE", summary.status());
        assertFalse(summary.emailVerified());
        assertNotNull(summary.createdAt());
        assertNotNull(summary.updatedAt());
    }

    @Test
    void toAuthResponse_shouldBuildBearerResponse() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.fromString("aaf4f9f9-7c12-4db1-b5af-995e2f6a8968"));
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-05-27T09:00:00Z"));
        ReflectionTestUtils.setField(user, "updatedAt", Instant.parse("2026-05-27T09:30:00Z"));
        user.setFullName("Demo User");
        user.setEmail("demo@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        AuthResponse response = authMapper.toAuthResponse("jwt-token", 3600L, user);

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresInSeconds());
        assertEquals("demo@example.com", response.user().email());
        assertEquals("ACTIVE", response.user().status());
    }
}
