package com.gamifiedstudyhub.backend.auth.repository;

import com.gamifiedstudyhub.backend.auth.entity.EmailVerificationToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findByTokenHashAndConsumedAtIsNull(String tokenHash);

    List<EmailVerificationToken> findByUser_IdAndConsumedAtIsNull(UUID userId);

    void deleteByUser_Id(UUID userId);
}
