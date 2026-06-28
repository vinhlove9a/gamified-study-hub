package com.gamifiedstudyhub.backend.mfa.controller;

import com.gamifiedstudyhub.backend.audit.AuthEventType;
import com.gamifiedstudyhub.backend.audit.service.AuthAuditService;
import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.auth.service.AuthService;
import com.gamifiedstudyhub.backend.auth.service.AuthSessionService;
import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.common.exception.UnauthorizedException;
import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import com.gamifiedstudyhub.backend.mfa.MfaChallengeService;
import com.gamifiedstudyhub.backend.mfa.MfaService;
import com.gamifiedstudyhub.backend.mfa.dto.MfaDtos;
import com.gamifiedstudyhub.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_V1_PREFIX + "/auth/mfa")
public class MfaController {

    private final MfaService mfaService;
    private final MfaChallengeService mfaChallengeService;
    private final AuthService authService;
    private final AuthSessionService authSessionService;
    private final AuthAuditService auditService;

    public MfaController(
            MfaService mfaService,
            MfaChallengeService mfaChallengeService,
            AuthService authService,
            AuthSessionService authSessionService,
            AuthAuditService auditService
    ) {
        this.mfaService = mfaService;
        this.mfaChallengeService = mfaChallengeService;
        this.authService = authService;
        this.authSessionService = authSessionService;
        this.auditService = auditService;
    }

    /** Step-up verification during login (no session yet — uses the MFA challenge token). */
    @PostMapping("/verify")
    @Operation(summary = "Complete login with an MFA code", security = {})
    public ApiResponse<AuthResponse> verify(
            @Valid @RequestBody MfaDtos.MfaVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        RequestMetadata meta = RequestMetadata.from(httpRequest);
        UUID userId = mfaChallengeService.consume(request.mfaToken());
        if (userId == null) {
            throw new BusinessException(ErrorCodes.AUTH_MFA_CHALLENGE_INVALID,
                    "MFA challenge is invalid or expired", HttpStatus.UNAUTHORIZED);
        }
        if (!mfaService.verifyCode(userId, request.code())) {
            auditService.record(AuthEventType.MFA_FAILED, userId, meta);
            throw new BusinessException(ErrorCodes.AUTH_MFA_INVALID_CODE,
                    "Invalid authentication code", HttpStatus.UNAUTHORIZED);
        }
        auditService.record(AuthEventType.MFA_VERIFIED, userId, meta);
        AuthResponse response = authService.completeLogin(userId, meta);
        authSessionService.issueSession(httpResponse, response.user().id(), response.accessToken());
        return ApiResponse.success("Login successful", response);
    }

    @GetMapping("/status")
    @Operation(summary = "Whether MFA is enabled for the current user",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<MfaDtos.MfaStatusResponse> status() {
        boolean enabled = mfaService.isEnabled(currentUser().getId());
        return ApiResponse.success("MFA status", new MfaDtos.MfaStatusResponse(enabled));
    }

    @PostMapping("/enroll")
    @Operation(summary = "Begin MFA enrollment (returns secret + QR)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<MfaDtos.EnrollResponse> enroll() {
        return ApiResponse.success("MFA enrollment started", mfaService.enroll(currentUser()));
    }

    @PostMapping("/verify-enroll")
    @Operation(summary = "Confirm MFA enrollment and get recovery codes",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<MfaDtos.VerifyEnrollResponse> verifyEnroll(
            @Valid @RequestBody MfaDtos.VerifyEnrollRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = currentUser();
        List<String> recoveryCodes = mfaService.verifyEnroll(user, request.code());
        auditService.record(AuthEventType.MFA_ENROLLED, user.getId(), RequestMetadata.from(httpRequest));
        return ApiResponse.success("MFA enabled", new MfaDtos.VerifyEnrollResponse(recoveryCodes));
    }

    @DeleteMapping
    @Operation(summary = "Disable MFA (requires a valid code)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<Void> disable(@Valid @RequestBody MfaDtos.DisableRequest request) {
        mfaService.disable(currentUser(), request.code());
        return ApiResponse.success("MFA disabled", null);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUser();
        }
        throw new UnauthorizedException("Unauthorized");
    }
}
