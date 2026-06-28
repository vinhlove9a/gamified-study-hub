package com.gamifiedstudyhub.backend.mfa.repository;

import com.gamifiedstudyhub.backend.mfa.entity.UserMfaRecoveryCode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMfaRecoveryCodeRepository extends JpaRepository<UserMfaRecoveryCode, UUID> {

    List<UserMfaRecoveryCode> findByUserMfaIdAndConsumedAtIsNull(UUID userMfaId);

    void deleteByUserMfaId(UUID userMfaId);
}
