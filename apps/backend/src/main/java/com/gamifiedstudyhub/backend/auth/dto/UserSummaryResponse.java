package com.gamifiedstudyhub.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String fullName,
        String email,
        String status,
        boolean emailVerified,
        Instant createdAt,
        Instant updatedAt
) {
}
