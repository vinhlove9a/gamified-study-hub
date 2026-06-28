package com.gamifiedstudyhub.backend.mfa.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/** MFA request/response DTOs. */
public final class MfaDtos {

    private MfaDtos() {
    }

    public record EnrollResponse(String secret, String otpauthUri, String qrDataUri) {
    }

    public record VerifyEnrollRequest(@NotBlank String code) {
    }

    public record VerifyEnrollResponse(List<String> recoveryCodes) {
    }

    public record MfaVerifyRequest(@NotBlank String mfaToken, @NotBlank String code) {
    }

    public record DisableRequest(@NotBlank String code) {
    }

    /** Returned by /login when the account has MFA enabled. */
    public record MfaChallengeResponse(boolean mfaRequired, String mfaToken) {
    }

    public record MfaStatusResponse(boolean enabled) {
    }
}
