package com.gamifiedstudyhub.backend.auth.controller;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.AuthMessageResponse;
import com.gamifiedstudyhub.backend.auth.dto.ChangePasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.ForgotPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.LoginRequest;
import com.gamifiedstudyhub.backend.auth.dto.RegisterRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResendVerificationRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResetPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.UpdateProfileRequest;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.auth.dto.VerifyEmailRequest;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.auth.service.AuthLoginResult;
import com.gamifiedstudyhub.backend.auth.service.AuthService;
import com.gamifiedstudyhub.backend.auth.service.AuthSessionService;
import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import com.gamifiedstudyhub.backend.mfa.MfaChallengeService;
import com.gamifiedstudyhub.backend.mfa.dto.MfaDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_V1_PREFIX + "/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthSessionService authSessionService;
    private final MfaChallengeService mfaChallengeService;

    public AuthController(
            AuthService authService,
            AuthSessionService authSessionService,
            MfaChallengeService mfaChallengeService
    ) {
        this.authService = authService;
        this.authSessionService = authSessionService;
        this.mfaChallengeService = mfaChallengeService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", security = {})
    public ApiResponse<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.register(request);
        authSessionService.issueSession(httpResponse, response.user().id(), response.accessToken());
        return ApiResponse.success("User registered successfully", response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password", security = {})
    public ApiResponse<Object> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        RequestMetadata meta = RequestMetadata.from(httpRequest);
        AuthLoginResult result = authService.login(request, meta);
        if (result instanceof AuthLoginResult.MfaRequired mfaRequired) {
            String mfaToken = mfaChallengeService.issue(mfaRequired.userId());
            return ApiResponse.success("MFA required", new MfaDtos.MfaChallengeResponse(true, mfaToken));
        }
        AuthResponse response = ((AuthLoginResult.Success) result).response();
        authSessionService.issueSession(httpResponse, response.user().id(), response.accessToken());
        return ApiResponse.success("Login successful", response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate the session using the refresh cookie", security = {})
    public ApiResponse<UserSummaryResponse> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        UserSummaryResponse user = authSessionService.refresh(
                httpRequest, httpResponse, RequestMetadata.from(httpRequest));
        return ApiResponse.success("Session refreshed", user);
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out: revoke the refresh token and clear cookies", security = {})
    public ApiResponse<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authSessionService.logout(httpRequest, httpResponse, currentUserId(), RequestMetadata.from(httpRequest));
        return ApiResponse.success("Logged out", null);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset instructions", security = {})
    public ApiResponse<AuthMessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthMessageResponse response = authService.forgotPassword(request, RequestMetadata.from(httpRequest));
        return ApiResponse.success("Forgot password request accepted", response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with a valid reset token", security = {})
    public ApiResponse<AuthMessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AuthMessageResponse response = authService.resetPassword(request);
        return ApiResponse.success("Password reset completed", response);
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify user email with a valid verification token", security = {})
    public ApiResponse<AuthMessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthMessageResponse response = authService.verifyEmail(request);
        return ApiResponse.success("Email verification completed", response);
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Request a new email verification link", security = {})
    public ApiResponse<AuthMessageResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthMessageResponse response = authService.resendVerification(request, RequestMetadata.from(httpRequest));
        return ApiResponse.success("Resend verification request accepted", response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<UserSummaryResponse> me() {
        UserSummaryResponse response = authService.getCurrentUser();
        return ApiResponse.success("Current user retrieved", response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user's profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<UserSummaryResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserSummaryResponse response = authService.updateProfile(currentUserId(), request);
        return ApiResponse.success("Profile updated", response);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password (revokes all other sessions)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletResponse httpResponse
    ) {
        UUID userId = currentUserId();
        AuthResponse response = authService.changePassword(userId, request);
        // changePassword revoked every session; mint a fresh one so this device stays logged in.
        authSessionService.issueSession(httpResponse, userId, response.accessToken());
        return ApiResponse.success("Password changed", null);
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Log out of all devices (revoke every session)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<Void> logoutAll(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authSessionService.logoutAll(httpResponse, currentUserId(), RequestMetadata.from(httpRequest));
        return ApiResponse.success("Logged out of all devices", null);
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUserId();
        }
        return null;
    }
}
