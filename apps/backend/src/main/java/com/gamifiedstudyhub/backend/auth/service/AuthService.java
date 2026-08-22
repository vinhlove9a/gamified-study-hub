package com.gamifiedstudyhub.backend.auth.service;

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
import com.gamifiedstudyhub.backend.auth.mapper.AuthMapper;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.auth.token.RefreshTokenService;
import com.gamifiedstudyhub.backend.audit.AuthEventType;
import com.gamifiedstudyhub.backend.audit.service.AuthAuditService;
import com.gamifiedstudyhub.backend.auth.ratelimit.EmailRateLimiter;
import com.gamifiedstudyhub.backend.auth.ratelimit.LoginRateLimiter;
import com.gamifiedstudyhub.backend.authz.service.AuthorityService;
import com.gamifiedstudyhub.backend.email.EmailService;
import com.gamifiedstudyhub.backend.mfa.MfaService;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.common.exception.UnauthorizedException;
import com.gamifiedstudyhub.backend.common.util.DateTimeUtils;
import com.gamifiedstudyhub.backend.common.web.RequestMetadata;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import com.gamifiedstudyhub.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final AuthTokenService authTokenService;
    private final AuthorityService authorityService;
    private final LoginRateLimiter loginRateLimiter;
    private final EmailRateLimiter emailRateLimiter;
    private final AuthAuditService auditService;
    private final EmailService emailService;
    private final MfaService mfaService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthMapper authMapper,
            PasswordPolicyValidator passwordPolicyValidator,
            AuthTokenService authTokenService,
            AuthorityService authorityService,
            LoginRateLimiter loginRateLimiter,
            EmailRateLimiter emailRateLimiter,
            AuthAuditService auditService,
            EmailService emailService,
            MfaService mfaService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.authTokenService = authTokenService;
        this.authorityService = authorityService;
        this.loginRateLimiter = loginRateLimiter;
        this.emailRateLimiter = emailRateLimiter;
        this.auditService = auditService;
        this.emailService = emailService;
        this.mfaService = mfaService;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        passwordPolicyValidator.validateOrThrow(request.password());
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(email)) {
            throw new BusinessException(
                    ErrorCodes.EMAIL_ALREADY_EXISTS,
                    "Email already exists",
                    HttpStatus.CONFLICT
            );
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setEmailVerifiedAt(null);

        User savedUser = userRepository.save(user);
        authorityService.assignDefaultRole(savedUser.getId());

        // Send the email-verification link (best-effort, async).
        AuthTokenIssueResult verification = authTokenService.createEmailVerificationToken(savedUser);
        emailService.sendVerificationEmail(savedUser, verification.rawToken());
        auditService.record(AuthEventType.REGISTERED, savedUser.getId(), null);

        String accessToken = jwtService.generateAccessToken(savedUser);
        List<String> authorities = authorityService.resolveAuthorityCodes(savedUser.getId());

        return authMapper.toAuthResponse(
                accessToken, jwtService.getAccessTokenExpirationSeconds(), savedUser, authorities);
    }

    public AuthLoginResult login(LoginRequest request, RequestMetadata meta) {
        String email = normalizeEmail(request.email());

        // Throttle / lockout BEFORE doing any password work (throws 429 if blocked).
        loginRateLimiter.assertAllowed(meta.ip(), email);

        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email).orElse(null);

        boolean badCredentials = user == null
                || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash());
        if (badCredentials) {
            loginRateLimiter.recordFailure(email);
            UUID userId = user == null ? null : user.getId();
            AuthEventType type = loginRateLimiter.isLocked(email)
                    ? AuthEventType.ACCOUNT_LOCKED : AuthEventType.LOGIN_FAILURE;
            auditService.record(type, userId, meta, "email=" + email);
            throw invalidCredentialsException();
        }

        if (UserStatus.DISABLED.equals(user.getStatus())) {
            throw new BusinessException(
                    ErrorCodes.USER_DISABLED,
                    "User account is disabled",
                    HttpStatus.FORBIDDEN
            );
        }

        if (UserStatus.PENDING.equals(user.getStatus())) {
            throw new BusinessException(
                    ErrorCodes.USER_PENDING,
                    "User account is pending activation",
                    HttpStatus.FORBIDDEN
            );
        }

        // Password is correct → clear failure counters.
        loginRateLimiter.recordSuccess(email);

        // Second factor required? Defer session issuance until MFA is verified.
        if (mfaService.isEnabled(user.getId())) {
            return new AuthLoginResult.MfaRequired(user.getId());
        }

        return new AuthLoginResult.Success(finalizeLogin(user, meta));
    }

    /** Completes a login (after password and any MFA step): stamps last-login, audits, mints token. */
    @Transactional
    public AuthResponse completeLogin(UUID userId, RequestMetadata meta) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(this::invalidCredentialsException);
        return finalizeLogin(user, meta);
    }

    private AuthResponse finalizeLogin(User user, RequestMetadata meta) {
        user.setLastLoginAt(DateTimeUtils.nowUtc());
        userRepository.save(user);
        auditService.record(AuthEventType.LOGIN_SUCCESS, user.getId(), meta);

        String accessToken = jwtService.generateAccessToken(user);
        List<String> authorities = authorityService.resolveAuthorityCodes(user.getId());
        return authMapper.toAuthResponse(
                accessToken, jwtService.getAccessTokenExpirationSeconds(), user, authorities);
    }

    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Unauthorized");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails customUserDetails)) {
            throw new UnauthorizedException("Unauthorized");
        }

        List<String> authorities = customUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return authMapper.toUserSummary(customUserDetails.getUser(), authorities);
    }

    public AuthMessageResponse forgotPassword(ForgotPasswordRequest request, RequestMetadata meta) {
        String email = normalizeEmail(request.email());

        // Throttle BEFORE the lookup so the limit applies regardless of whether the
        // address exists (prevents inbox bombing / enumeration). Throws 429 if blocked.
        emailRateLimiter.assertAllowed(meta.ip(), email);

        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(user -> !UserStatus.DISABLED.equals(user.getStatus()))
                .ifPresent(user -> {
                    AuthTokenIssueResult reset = authTokenService.createPasswordResetToken(user);
                    emailService.sendPasswordResetEmail(user, reset.rawToken());
                    auditService.record(AuthEventType.PASSWORD_RESET_REQUESTED, user.getId(), null);
                });

        return new AuthMessageResponse(
                "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu sẽ được gửi đến email của bạn."
        );
    }

    public AuthMessageResponse resetPassword(ResetPasswordRequest request) {
        passwordPolicyValidator.validateOrThrow(request.newPassword());

        User user = authTokenService.consumePasswordResetToken(request.token()).getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        // A reset implies the account may be compromised — invalidate every existing session.
        refreshTokenService.revokeAllForUser(user.getId());
        auditService.record(AuthEventType.PASSWORD_RESET_COMPLETED, user.getId(), null);

        return new AuthMessageResponse("Mật khẩu đã được cập nhật. Bạn có thể đăng nhập bằng mật khẩu mới.");
    }

    /**
     * Change the password of the currently authenticated user. Verifies the current password,
     * enforces the strength policy, forbids reusing the same password, then revokes every
     * existing session (the caller re-issues a fresh one for this device). Returns a new
     * access token so the current request stays authenticated.
     */
    public AuthResponse changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new BusinessException(
                    ErrorCodes.BAD_REQUEST,
                    "No password is set for this account. Use the password reset flow to set one.",
                    HttpStatus.BAD_REQUEST
            );
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            auditService.record(AuthEventType.LOGIN_FAILURE, userId, null, "change-password: wrong current password");
            throw new BusinessException(
                    ErrorCodes.INVALID_CREDENTIALS,
                    "Current password is incorrect",
                    HttpStatus.BAD_REQUEST
            );
        }
        passwordPolicyValidator.validateOrThrow(request.newPassword());
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(
                    ErrorCodes.AUTH_PASSWORD_WEAK,
                    "New password must be different from the current password",
                    HttpStatus.BAD_REQUEST
            );
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        // Revoke all sessions (incl. the current one); the controller mints a fresh session next.
        refreshTokenService.revokeAllForUser(userId);
        auditService.record(AuthEventType.PASSWORD_CHANGED, userId, null);

        String accessToken = jwtService.generateAccessToken(user);
        List<String> authorities = authorityService.resolveAuthorityCodes(userId);
        return authMapper.toAuthResponse(
                accessToken, jwtService.getAccessTokenExpirationSeconds(), user, authorities);
    }

    /** Update the current user's profile (partial): null fields are left unchanged. */
    public UserSummaryResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("Unauthorized"));

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.avatarUrl() != null) {
            String trimmed = request.avatarUrl().trim();
            user.setAvatarUrl(trimmed.isEmpty() ? null : trimmed);
        }
        userRepository.save(user);
        auditService.record(AuthEventType.PROFILE_UPDATED, userId, null);

        List<String> authorities = authorityService.resolveAuthorityCodes(userId);
        return authMapper.toUserSummary(user, authorities);
    }

    public AuthMessageResponse verifyEmail(VerifyEmailRequest request) {
        User user = authTokenService.consumeEmailVerificationToken(request.token()).getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(DateTimeUtils.nowUtc());
        userRepository.save(user);
        auditService.record(AuthEventType.EMAIL_VERIFIED, user.getId(), null);

        return new AuthMessageResponse("Email đã được xác thực thành công.");
    }

    public AuthMessageResponse resendVerification(ResendVerificationRequest request, RequestMetadata meta) {
        String email = normalizeEmail(request.email());

        // Throttle BEFORE the lookup so the limit applies regardless of whether the
        // address exists (prevents inbox bombing / enumeration). Throws 429 if blocked.
        emailRateLimiter.assertAllowed(meta.ip(), email);

        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(user -> !user.isEmailVerified())
                .filter(user -> !UserStatus.DISABLED.equals(user.getStatus()))
                .ifPresent(user -> {
                    AuthTokenIssueResult verification = authTokenService.createEmailVerificationToken(user);
                    emailService.sendVerificationEmail(user, verification.rawToken());
                });

        return new AuthMessageResponse(
                "Nếu email tồn tại và chưa được xác thực, liên kết xác thực mới sẽ được gửi đến email của bạn."
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private BusinessException invalidCredentialsException() {
        return new BusinessException(
                ErrorCodes.INVALID_CREDENTIALS,
                "Invalid email or password",
                HttpStatus.UNAUTHORIZED
        );
    }
}
