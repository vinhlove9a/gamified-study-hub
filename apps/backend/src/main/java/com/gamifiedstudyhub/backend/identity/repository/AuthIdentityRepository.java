package com.gamifiedstudyhub.backend.identity.repository;

import com.gamifiedstudyhub.backend.identity.entity.AuthIdentity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthIdentityRepository extends JpaRepository<AuthIdentity, UUID> {

    Optional<AuthIdentity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
