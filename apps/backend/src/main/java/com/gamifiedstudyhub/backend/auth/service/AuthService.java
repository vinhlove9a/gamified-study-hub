package com.gamifiedstudyhub.backend.auth.service;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.AuthMessageResponse;
import com.gamifiedstudyhub.backend.auth.dto.ForgotPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.LoginRequest;
import com.gamifiedstudyhub.backend.auth.dto.RegisterRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResendVerificationRequest;
import com.gamifiedstudyhub.backend.auth.dto.ResetPasswordRequest;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
import com.gamifiedstudyhub.backend.auth.dto.VerifyEmailRequest;
import com.gamifiedstudyhub.backend.auth.mapper.AuthMapper;
import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.auth.security.JwtService;
import com.gamifiedstudyhub.backend.audit.AuthEventType;
import com.gamifiedstudyhub.backend.audit.service.AuthAuditService;
import com.gamifiedstudyhub.backend.auth.ratelimit.LoginRateLimiter;
import com.gamifiedstudyhub.backend.authz.service.AuthorityService;
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
    private final AuthAuditService auditService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthMapper authMapper,
            PasswordPolicyValidator passwordPolicyValidator,
            AuthTokenService authTokenService,
            AuthorityService authorityService,
            LoginRateLimiter loginRateLimiter,
            AuthAuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.authTokenService = authTokenService;
        this.authorityService = authorityService;
        this.loginRateLimiter = loginRateLimiter;
        this.auditService = auditService;
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

        String accessToken = jwtService.generateAccessToken(savedUser);
        List<String> authorities = authorityService.resolveAuthorityCodes(savedUser.getId());

        return authMapper.toAuthResponse(
                accessToken, jwtService.getAccessTokenExpirationSeconds(), savedUser, authorities);
    }

    public AuthResponse login(LoginRequest request, RequestMetadata meta) {
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

        user.setLastLoginAt(DateTimeUtils.nowUtc());
        userRepository.save(user);
        loginRateLimiter.recordSuccess(email);
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

    public AuthMessageResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());

        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(user -> !UserStatus.DISABLED.equals(user.getStatus()))
                .ifPresent(authTokenService::createPasswordResetToken);

        return new AuthMessageResponse(
                "Nếu email tồn tại trong hệ thống, hướng dẫn đặt lại mật khẩu sẽ được gửi đến email của bạn."
        );
    }

    public AuthMessageResponse resetPassword(ResetPasswordRequest request) {
        passwordPolicyValidator.validateOrThrow(request.newPassword());

        User user = authTokenService.consumePasswordResetToken(request.token()).getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return new AuthMessageResponse("Mật khẩu đã được cập nhật. Bạn có thể đăng nhập bằng mật khẩu mới.");
    }

    public AuthMessageResponse verifyEmail(VerifyEmailRequest request) {
        User user = authTokenService.consumeEmailVerificationToken(request.token()).getUser();
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(DateTimeUtils.nowUtc());
        userRepository.save(user);

        return new AuthMessageResponse("Email đã được xác thực thành công.");
    }

    public AuthMessageResponse resendVerification(ResendVerificationRequest request) {
        String email = normalizeEmail(request.email());

        userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(user -> !user.isEmailVerified())
                .filter(user -> !UserStatus.DISABLED.equals(user.getStatus()))
                .ifPresent(authTokenService::createEmailVerificationToken);

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
