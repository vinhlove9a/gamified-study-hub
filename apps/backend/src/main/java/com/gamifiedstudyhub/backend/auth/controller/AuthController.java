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
import com.gamifiedstudyhub.backend.auth.service.AuthService;
import com.gamifiedstudyhub.backend.common.constant.AppConstants;
import com.gamifiedstudyhub.backend.common.response.ApiResponse;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AppConstants.API_V1_PREFIX + "/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", security = {})
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ApiResponse.success("User registered successfully", response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password", security = {})
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse response = authService.login(request, RequestMetadata.from(httpRequest));
        return ApiResponse.success("Login successful", response);
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
}
