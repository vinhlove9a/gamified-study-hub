package com.gamifiedstudyhub.backend.authz.service;

import com.gamifiedstudyhub.backend.authz.entity.Role;
import com.gamifiedstudyhub.backend.authz.entity.UserRole;
import com.gamifiedstudyhub.backend.authz.entity.UserRoleId;
import com.gamifiedstudyhub.backend.authz.repository.RoleRepository;
import com.gamifiedstudyhub.backend.authz.repository.UserRoleRepository;
import com.gamifiedstudyhub.backend.common.util.DateTimeUtils;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Resolves and grants the global RBAC authorities (roles + permissions) for a user. */
@Service
public class AuthorityService {

    public static final String DEFAULT_ROLE_CODE = "ROLE_USER";

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public AuthorityService(UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    /** Authority code strings: {@code ROLE_*} plus permission codes. */
    @Transactional(readOnly = true)
    public List<String> resolveAuthorityCodes(UUID userId) {
        return userRoleRepository.findAuthorityCodesByUserId(userId);
    }

    /** Same as {@link #resolveAuthorityCodes} but as Spring Security authorities. */
    @Transactional(readOnly = true)
    public List<GrantedAuthority> resolveAuthorities(UUID userId) {
        return resolveAuthorityCodes(userId).stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority(code))
                .toList();
    }

    /** Idempotently grant the default {@code ROLE_USER} to a (newly created) user. */
    @Transactional
    public void assignDefaultRole(UUID userId) {
        Role userRole = roleRepository.findByCode(DEFAULT_ROLE_CODE).orElse(null);
        if (userRole == null) {
            return; // role table not seeded (e.g. in a slice test) — nothing to grant
        }
        UserRoleId id = new UserRoleId(userId, userRole.getId());
        if (!userRoleRepository.existsById(id)) {
            userRoleRepository.save(new UserRole(id, DateTimeUtils.nowUtc(), null));
        }
    }
}
