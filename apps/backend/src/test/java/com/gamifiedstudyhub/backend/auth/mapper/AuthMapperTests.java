package com.gamifiedstudyhub.backend.auth.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthMapperTests {

    private final AuthMapper authMapper = new AuthMapper();

    @Test
    void toUserSummary_shouldMapSafeFieldsAndDeriveRole() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.fromString("6adf5f43-5f4a-4ebf-b305-e32a2b1cb54e"));
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-05-27T09:00:00Z"));
        ReflectionTestUtils.setField(user, "updatedAt", Instant.parse("2026-05-27T09:30:00Z"));
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setPasswordHash("secret-hash");

        UserSummaryResponse summary = authMapper.toUserSummary(user, List.of("ROLE_USER", "DOCUMENT_READ"));

        assertEquals(user.getId(), summary.id());
        assertEquals("Test User", summary.fullName());
        assertEquals("test@example.com", summary.email());
        assertEquals("ACTIVE", summary.status());
        assertFalse(summary.emailVerified());
        assertEquals("user", summary.role());
        assertTrue(summary.roles().contains("ROLE_USER"));
        assertTrue(summary.permissions().contains("DOCUMENT_READ"));
        assertNotNull(summary.createdAt());
        assertNotNull(summary.updatedAt());
    }

    @Test
    void toUserSummary_shouldMapAdminRole() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "createdAt", Instant.parse("2026-05-27T09:00:00Z"));
        ReflectionTestUtils.setField(user, "updatedAt", Instant.parse("2026-05-27T09:30:00Z"));
        user.setFullName("Admin User");
        user.setEmail("admin@example.com");
        user.setStatus(UserStatus.ACTIVE);

        UserSummaryResponse summary = authMapper.toUserSummary(user, List.of("ROLE_ADMIN", "ADMIN_PANEL"));

        assertEquals("admin", summary.role());
        assertTrue(summary.roles().contains("ROLE_ADMIN"));
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

        AuthResponse response = authMapper.toAuthResponse("jwt-token", 3600L, user, List.of("ROLE_USER"));

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresInSeconds());
        assertEquals("demo@example.com", response.user().email());
        assertEquals("ACTIVE", response.user().status());
        assertEquals("user", response.user().role());
    }
}
