package com.gamifiedstudyhub.backend.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Partial profile update: any field left {@code null} is unchanged. A blank {@code avatarUrl}
 * clears the avatar.
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 255, message = "Full name must be between 1 and 255 characters")
        String fullName,

        @Size(max = 1024, message = "Avatar URL is too long")
        String avatarUrl
) {
}
