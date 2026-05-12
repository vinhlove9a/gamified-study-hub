package com.gamifiedstudyhub.backend.auth.dto;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        String status
) {
}
