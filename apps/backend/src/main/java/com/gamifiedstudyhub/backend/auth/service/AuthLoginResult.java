package com.gamifiedstudyhub.backend.auth.service;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import java.util.UUID;

/** Outcome of a password login: either a full session, or an MFA challenge. */
public sealed interface AuthLoginResult permits AuthLoginResult.Success, AuthLoginResult.MfaRequired {

    record Success(AuthResponse response) implements AuthLoginResult {
    }

    record MfaRequired(UUID userId) implements AuthLoginResult {
    }
}
