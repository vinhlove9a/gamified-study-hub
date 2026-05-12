package com.gamifiedstudyhub.backend.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        CurrentUserResponse user
) {
}
