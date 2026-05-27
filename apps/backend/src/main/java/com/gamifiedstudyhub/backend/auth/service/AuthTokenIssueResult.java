package com.gamifiedstudyhub.backend.auth.service;

import java.time.Instant;

public record AuthTokenIssueResult(
        String rawToken,
        String tokenHash,
        Instant expiresAt
) {
}
