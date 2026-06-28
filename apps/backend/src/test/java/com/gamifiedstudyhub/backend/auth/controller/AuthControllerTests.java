package com.gamifiedstudyhub.backend.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gamifiedstudyhub.backend.auth.dto.AuthMessageResponse;
import com.gamifiedstudyhub.backend.auth.dto.ForgotPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResendVerificationRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResetPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.VerifyEmailRequest;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetailsService;
import com.gamifiedstudyhub.backend.auth.security.JwtAuthenticationEntryPoint;
import com.gamifiedstudyhub.backend.auth.security.JwtAuthenticationFilter;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.auth.security.RestAccessDeniedHandler;
import com.gamifiedstudyhub.backend.auth.service.AuthService;
import com.gamifiedstudyhub.backend.auth.service.AuthSessionService;
import com.gamifiedstudyhub.backend.common.config.SecurityConfig;
import com.gamifiedstudyhub.backend.identity.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.gamifiedstudyhub.backend.identity.oauth.OAuth2LoginFailureHandler;
import com.gamifiedstudyhub.backend.identity.oauth.OAuth2LoginSuccessHandler;
import com.gamifiedstudyhub.backend.mfa.MfaChallengeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class})
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthSessionService authSessionService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private MfaChallengeService mfaChallengeService;

    @MockBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @MockBean
    private OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @MockBean
    private HttpCookieOAuth2AuthorizationRequestRepository oAuth2AuthorizationRequestRepository;

    @Test
    void forgotPassword_shouldReturnSuccessForValidEmail() throws Exception {
        when(authService.forgotPassword(any(ForgotPasswordRequest.class)))
                .thenReturn(new AuthMessageResponse("forgot-password-accepted"));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("forgot-password-accepted"));

        verify(authService).forgotPassword(any(ForgotPasswordRequest.class));
    }

    @Test
    void resetPassword_shouldReturnSuccessForValidRequest() throws Exception {
        when(authService.resetPassword(any(ResetPasswordRequest.class)))
                .thenReturn(new AuthMessageResponse("password-reset-success"));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(APPLICATION_JSON)
                        .content("{\"token\":\"valid-token\",\"newPassword\":\"NewPassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("password-reset-success"));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }

    @Test
    void verifyEmail_shouldReturnSuccessForValidRequest() throws Exception {
        when(authService.verifyEmail(any(VerifyEmailRequest.class)))
                .thenReturn(new AuthMessageResponse("email-verified"));

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .contentType(APPLICATION_JSON)
                        .content("{\"token\":\"verify-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("email-verified"));

        verify(authService).verifyEmail(any(VerifyEmailRequest.class));
    }

    @Test
    void resendVerification_shouldReturnSuccessForValidEmail() throws Exception {
        when(authService.resendVerification(any(ResendVerificationRequest.class)))
                .thenReturn(new AuthMessageResponse("resend-accepted"));

        mockMvc.perform(post("/api/v1/auth/resend-verification")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("resend-accepted"));

        verify(authService).resendVerification(any(ResendVerificationRequest.class));
    }

    @Test
    void me_shouldRemainProtectedWithoutBearerToken() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        verify(authService, never()).getCurrentUser();
    }
}
