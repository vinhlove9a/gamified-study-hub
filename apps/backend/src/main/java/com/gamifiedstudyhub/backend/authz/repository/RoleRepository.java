package com.gamifiedstudyhub.backend.authz.repository;

import com.gamifiedstudyhub.backend.authz.entity.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByCode(String code);
}
