package com.gamifiedstudyhub.backend.auth.service;

import com.gamifiedstudyhub.backend.auth.dto.AuthResponse;
import com.gamifiedstudyhub.backend.auth.dto.LoginRequest;
import com.gamifiedstudyhub.backend.auth.dto.RegisterRequest;
import com.gamifiedstudyhub.backend.auth.dto.UserSummaryResponse;
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

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthMapper authMapper,
            PasswordPolicyValidator passwordPolicyValidator
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.passwordPolicyValidator = passwordPolicyValidator;
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
