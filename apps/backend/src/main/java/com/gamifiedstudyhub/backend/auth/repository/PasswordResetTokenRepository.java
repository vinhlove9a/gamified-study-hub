package com.gamifiedstudyhub.backend.auth.repository;

import com.gamifiedstudyhub.backend.auth.entity.PasswordResetToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    Optional<PasswordResetToken> findByTokenHashAndConsumedAtIsNull(String tokenHash);

    List<PasswordResetToken> findByUser_IdAndConsumedAtIsNull(UUID userId);

    void deleteByUser_Id(UUID userId);
}
