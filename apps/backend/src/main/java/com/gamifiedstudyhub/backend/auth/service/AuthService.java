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
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.common.exception.UnauthorizedException;
import com.gamifiedstudyhub.backend.common.util.DateTimeUtils;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.user.entity.UserStatus;
import com.gamifiedstudyhub.backend.user.repository.UserRepository;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthMapper authMapper,
            PasswordPolicyValidator passwordPolicyValidator,
            AuthTokenService authTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.passwordPolicyValidator = passwordPolicyValidator;
        this.authTokenService = authTokenService;
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
        String accessToken = jwtService.generateAccessToken(savedUser);

        return authMapper.toAuthResponse(accessToken, jwtService.getAccessTokenExpirationSeconds(), savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(this::invalidCredentialsException);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
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

        String accessToken = jwtService.generateAccessToken(user);
        return authMapper.toAuthResponse(accessToken, jwtService.getAccessTokenExpirationSeconds(), user);
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

        return authMapper.toUserSummary(customUserDetails.getUser());
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
