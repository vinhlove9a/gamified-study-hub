package com.gamifiedstudyhub.backend.auth.controller;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.AuthMessageResponse;
import com.gamifiedstudyhub.backend.auth.dto.ForgotPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.LoginRequest;
import com.gamifiedstudyhub.backend.auth.dto.RegisterRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResendVerificationRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResetPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.auth.dto.VerifyEmailRequest;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.auth.service.AuthService;
import com.gamifiedstudyhub.backend.auth.service.AuthSessionService;
import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_V1_PREFIX + "/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthSessionService authSessionService;

    public AuthController(AuthService authService, AuthSessionService authSessionService) {
        this.authService = authService;
        this.authSessionService = authSessionService;
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
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.login(request, RequestMetadata.from(httpRequest));
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
    public ApiResponse<AuthMessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        AuthMessageResponse response = authService.forgotPassword(request);
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
            @Valid @RequestBody ResendVerificationRequest request
    ) {
        AuthMessageResponse response = authService.resendVerification(request);
        return ApiResponse.success("Resend verification request accepted", response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<UserSummaryResponse> me() {
        UserSummaryResponse response = authService.getCurrentUser();
        return ApiResponse.success("Current user retrieved", response);
    }

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUserId();
        }
        return null;
    }
}
