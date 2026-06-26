package com.gamifiedstudyhub.backend.authz.repository;

import com.gamifiedstudyhub.backend.authz.entity.UserRole;
import com.gamifiedstudyhub.backend.authz.entity.UserRoleId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    /**
     * All Spring Security authority codes for a user: role codes ({@code ROLE_*})
     * plus the permission codes granted through those roles. Single round-trip.
     */
    @Query(value = """
            SELECT r.code FROM user_roles ur
              JOIN roles r ON r.id = ur.role_id
            WHERE ur.user_id = :userId
            UNION
            SELECT p.code FROM user_roles ur
              JOIN role_permissions rp ON rp.role_id = ur.role_id
              JOIN permissions p ON p.id = rp.permission_id
            WHERE ur.user_id = :userId
            """, nativeQuery = true)
    List<String> findAuthorityCodesByUserId(@Param("userId") UUID userId);
}
