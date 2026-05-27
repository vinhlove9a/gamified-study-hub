package com.gamifiedstudyhub.backend.auth.service;

import com.gamifiedstudyhub.backend.auth.entity.EmailVerificationToken;
import com.gamifiedstudyhub.backend.auth.entity.PasswordResetToken;
import com.gamifiedstudyhub.backend.auth.repository.EmailVerificationTokenRepository;
import com.gamifiedstudyhub.backend.auth.repository.PasswordResetTokenRepository;
import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.common.util.DateTimeUtils;
import com.gamifiedstudyhub.backend.user.entity.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;
    private static final long EMAIL_VERIFICATION_TTL_SECONDS = 24 * 60 * 60;
    private static final long PASSWORD_RESET_TTL_SECONDS = 30 * 60;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthTokenService(
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository
    ) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    public String generateRawToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    @Transactional
    public AuthTokenIssueResult createEmailVerificationToken(User user) {
        Instant now = DateTimeUtils.nowUtc();
        markEmailVerificationTokensConsumed(user.getId(), now);

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = now.plusSeconds(EMAIL_VERIFICATION_TTL_SECONDS);

        EmailVerificationToken entity = new EmailVerificationToken();
        entity.setUser(user);
        entity.setTokenHash(tokenHash);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(now);
        emailVerificationTokenRepository.save(entity);

        return new AuthTokenIssueResult(rawToken, tokenHash, expiresAt);
    }

    @Transactional
    public AuthTokenIssueResult createPasswordResetToken(User user) {
        Instant now = DateTimeUtils.nowUtc();
        markPasswordResetTokensConsumed(user.getId(), now);

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);
        Instant expiresAt = now.plusSeconds(PASSWORD_RESET_TTL_SECONDS);

        PasswordResetToken entity = new PasswordResetToken();
        entity.setUser(user);
        entity.setTokenHash(tokenHash);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(now);
        passwordResetTokenRepository.save(entity);

        return new AuthTokenIssueResult(rawToken, tokenHash, expiresAt);
    }

    @Transactional
    public EmailVerificationToken consumeEmailVerificationToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHashAndConsumedAtIsNull(tokenHash)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.AUTH_EMAIL_VERIFICATION_TOKEN_INVALID,
                        "Email verification token is invalid",
                        HttpStatus.BAD_REQUEST
                ));

        Instant now = DateTimeUtils.nowUtc();
        if (token.getExpiresAt().isBefore(now)) {
            throw new BusinessException(
                    ErrorCodes.AUTH_EXPIRED_TOKEN,
                    "Token has expired",
                    HttpStatus.BAD_REQUEST
            );
        }

        token.setConsumedAt(now);
        return emailVerificationTokenRepository.save(token);
    }

    @Transactional
    public PasswordResetToken consumePasswordResetToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashAndConsumedAtIsNull(tokenHash)
                .orElseThrow(() -> new BusinessException(
                        ErrorCodes.AUTH_PASSWORD_RESET_TOKEN_INVALID,
                        "Password reset token is invalid",
                        HttpStatus.BAD_REQUEST
                ));

        Instant now = DateTimeUtils.nowUtc();
        if (token.getExpiresAt().isBefore(now)) {
            throw new BusinessException(
                    ErrorCodes.AUTH_EXPIRED_TOKEN,
                    "Token has expired",
                    HttpStatus.BAD_REQUEST
            );
        }

        token.setConsumedAt(now);
        return passwordResetTokenRepository.save(token);
    }

    private void markEmailVerificationTokensConsumed(UUID userId, Instant consumedAt) {
        List<EmailVerificationToken> activeTokens = emailVerificationTokenRepository.findByUser_IdAndConsumedAtIsNull(userId);
        if (activeTokens.isEmpty()) {
            return;
        }

        activeTokens.forEach(token -> token.setConsumedAt(consumedAt));
        emailVerificationTokenRepository.saveAll(activeTokens);
    }

    private void markPasswordResetTokensConsumed(UUID userId, Instant consumedAt) {
        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findByUser_IdAndConsumedAtIsNull(userId);
        if (activeTokens.isEmpty()) {
            return;
        }

        activeTokens.forEach(token -> token.setConsumedAt(consumedAt));
        passwordResetTokenRepository.saveAll(activeTokens);
    }
}
