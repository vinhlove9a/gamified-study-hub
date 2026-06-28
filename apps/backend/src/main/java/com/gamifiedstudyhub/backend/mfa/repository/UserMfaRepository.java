package com.gamifiedstudyhub.backend.mfa.repository;

import com.gamifiedstudyhub.backend.mfa.entity.UserMfa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMfaRepository extends JpaRepository<UserMfa, UUID> {

    Optional<UserMfa> findByUserId(UUID userId);

    boolean existsByUserIdAndEnabledTrue(UUID userId);
}
