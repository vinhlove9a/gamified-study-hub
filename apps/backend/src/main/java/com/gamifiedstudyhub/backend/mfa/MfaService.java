package com.gamifiedstudyhub.backend.mfa;

import com.gamifiedstudyhub.backend.common.constant.ErrorCodes;
import com.gamifiedstudyhub.backend.common.exception.BusinessException;
import com.gamifiedstudyhub.backend.common.util.DateTimeUtils;
import com.gamifiedstudyhub.backend.mfa.dto.MfaDtos;
import com.gamifiedstudyhub.backend.mfa.entity.UserMfa;
import com.gamifiedstudyhub.backend.mfa.entity.UserMfaRecoveryCode;
import com.gamifiedstudyhub.backend.mfa.repository.UserMfaRecoveryCodeRepository;
import com.gamifiedstudyhub.backend.mfa.repository.UserMfaRepository;
import com.gamifiedstudyhub.backend.user.entity.User;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.util.Utils;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** TOTP enrollment, verification, recovery codes, and disable. */
@Service
public class MfaService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserMfaRepository userMfaRepository;
    private final UserMfaRecoveryCodeRepository recoveryCodeRepository;
    private final MfaCipher cipher;
    private final MfaProperties properties;
    private final PasswordEncoder passwordEncoder;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final CodeVerifier codeVerifier =
            new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    public MfaService(
            UserMfaRepository userMfaRepository,
            UserMfaRecoveryCodeRepository recoveryCodeRepository,
            MfaCipher cipher,
            MfaProperties properties,
            PasswordEncoder passwordEncoder
    ) {
        this.userMfaRepository = userMfaRepository;
        this.recoveryCodeRepository = recoveryCodeRepository;
        this.cipher = cipher;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public boolean isEnabled(UUID userId) {
        return userMfaRepository.existsByUserIdAndEnabledTrue(userId);
    }

    @Transactional
    public MfaDtos.EnrollResponse enroll(User user) {
        UserMfa mfa = userMfaRepository.findByUserId(user.getId()).orElse(null);
        if (mfa != null && mfa.isEnabled()) {
            throw new BusinessException(ErrorCodes.AUTH_MFA_ALREADY_ENROLLED,
                    "MFA is already enabled", HttpStatus.CONFLICT);
        }
        String secret = secretGenerator.generate();
        if (mfa == null) {
            mfa = new UserMfa();
            mfa.setUserId(user.getId());
            mfa.setCreatedAt(DateTimeUtils.nowUtc());
        }
        mfa.setSecretEncrypted(cipher.encrypt(secret));
        mfa.setEnabled(false);
        userMfaRepository.save(mfa);

        QrData qrData = new QrData.Builder()
                .label(user.getEmail() == null ? user.getId().toString() : user.getEmail())
                .secret(secret)
                .issuer(properties.getIssuer())
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        String qrDataUri;
        try {
            qrDataUri = Utils.getDataUriForImage(qrGenerator.generate(qrData), qrGenerator.getImageMimeType());
        } catch (Exception e) {
            throw new BusinessException(ErrorCodes.INTERNAL_SERVER_ERROR,
                    "Failed to generate QR code", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new MfaDtos.EnrollResponse(secret, qrData.getUri(), qrDataUri);
    }

    @Transactional
    public List<String> verifyEnroll(User user, String code) {
        UserMfa mfa = userMfaRepository.findByUserId(user.getId())
                .orElseThrow(() -> notEnrolled());
        String secret = cipher.decrypt(mfa.getSecretEncrypted());
        if (!codeVerifier.isValidCode(secret, code)) {
            throw invalidCode();
        }
        mfa.setEnabled(true);
        mfa.setEnrolledAt(DateTimeUtils.nowUtc());
        mfa.setLastUsedAt(DateTimeUtils.nowUtc());
        userMfaRepository.save(mfa);

        recoveryCodeRepository.deleteByUserMfaId(mfa.getId());
        List<String> plain = new ArrayList<>();
        for (int i = 0; i < properties.getRecoveryCodeCount(); i++) {
            String rc = randomRecoveryCode();
            plain.add(rc);
            UserMfaRecoveryCode entity = new UserMfaRecoveryCode();
            entity.setUserMfaId(mfa.getId());
            entity.setCodeHash(passwordEncoder.encode(rc));
            entity.setCreatedAt(DateTimeUtils.nowUtc());
            recoveryCodeRepository.save(entity);
        }
        return plain;
    }

    /** Verify a TOTP code or an unused recovery code (consuming it). */
    @Transactional
    public boolean verifyCode(UUID userId, String code) {
        UserMfa mfa = userMfaRepository.findByUserId(userId).filter(UserMfa::isEnabled).orElse(null);
        if (mfa == null || code == null || code.isBlank()) {
            return false;
        }
        String secret = cipher.decrypt(mfa.getSecretEncrypted());
        if (codeVerifier.isValidCode(secret, code.trim())) {
            mfa.setLastUsedAt(DateTimeUtils.nowUtc());
            userMfaRepository.save(mfa);
            return true;
        }
        for (UserMfaRecoveryCode rc : recoveryCodeRepository.findByUserMfaIdAndConsumedAtIsNull(mfa.getId())) {
            if (passwordEncoder.matches(code.trim(), rc.getCodeHash())) {
                rc.setConsumedAt(DateTimeUtils.nowUtc());
                recoveryCodeRepository.save(rc);
                mfa.setLastUsedAt(DateTimeUtils.nowUtc());
                userMfaRepository.save(mfa);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public void disable(User user, String code) {
        UserMfa mfa = userMfaRepository.findByUserId(user.getId())
                .filter(UserMfa::isEnabled)
                .orElseThrow(() -> notEnrolled());
        if (!verifyCode(user.getId(), code)) {
            throw invalidCode();
        }
        recoveryCodeRepository.deleteByUserMfaId(mfa.getId());
        userMfaRepository.delete(mfa);
    }

    private String randomRecoveryCode() {
        StringBuilder sb = new StringBuilder(11);
        for (int i = 0; i < 10; i++) {
            if (i == 5) {
                sb.append('-');
            }
            sb.append(CODE_ALPHABET.charAt(RANDOM.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    private BusinessException invalidCode() {
        return new BusinessException(ErrorCodes.AUTH_MFA_INVALID_CODE,
                "Invalid authentication code", HttpStatus.BAD_REQUEST);
    }

    private BusinessException notEnrolled() {
        return new BusinessException(ErrorCodes.AUTH_MFA_NOT_ENROLLED,
                "MFA is not set up", HttpStatus.BAD_REQUEST);
    }
}
