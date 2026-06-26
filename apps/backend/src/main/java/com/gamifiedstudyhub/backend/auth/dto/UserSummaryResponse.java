package com.gamifiedstudyhub.backend.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String fullName,
        String email,
        String status,
        boolean emailVerified,
        /** Simplified primary role for the SPA gate: "admin" or "user". */
        String role,
        /** Full role codes (e.g. ROLE_USER, ROLE_ADMIN). */
        List<String> roles,
        /** Granted permission codes (e.g. DOCUMENT_PUBLISH). */
        List<String> permissions,
        Instant createdAt,
        Instant updatedAt
) {
}
