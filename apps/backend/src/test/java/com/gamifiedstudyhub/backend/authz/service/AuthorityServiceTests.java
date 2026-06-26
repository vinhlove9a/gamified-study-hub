package com.gamifiedstudyhub.backend.authz.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.authz.entity.Role;
import com.gamifiedstudyhub.backend.authz.entity.UserRole;
import com.gamifiedstudyhub.backend.authz.entity.UserRoleId;
import com.gamifiedstudyhub.backend.authz.repository.RoleRepository;
import com.gamifiedstudyhub.backend.authz.repository.UserRoleRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthorityServiceTests {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthorityService authorityService;

    @Test
    void resolveAuthorities_shouldMapCodesToGrantedAuthorities() {
        UUID userId = UUID.randomUUID();
        when(userRoleRepository.findAuthorityCodesByUserId(userId))
                .thenReturn(List.of("ROLE_ADMIN", "ADMIN_PANEL"));

        List<GrantedAuthority> authorities = authorityService.resolveAuthorities(userId);

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(authorities.stream().anyMatch(a -> a.getAuthority().equals("ADMIN_PANEL")));
    }

    @Test
    void assignDefaultRole_shouldSaveWhenNotPresent() {
        UUID userId = UUID.randomUUID();
        Role role = new Role();
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        role.setCode("ROLE_USER");
        when(roleRepository.findByCode("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.existsById(any(UserRoleId.class))).thenReturn(false);

        authorityService.assignDefaultRole(userId);

        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void assignDefaultRole_shouldBeIdempotent() {
        UUID userId = UUID.randomUUID();
        Role role = new Role();
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        role.setCode("ROLE_USER");
        when(roleRepository.findByCode("ROLE_USER")).thenReturn(Optional.of(role));
        when(userRoleRepository.existsById(any(UserRoleId.class))).thenReturn(true);

        authorityService.assignDefaultRole(userId);

        verify(userRoleRepository, never()).save(any(UserRole.class));
    }

    @Test
    void assignDefaultRole_shouldNoOpWhenRoleMissing() {
        UUID userId = UUID.randomUUID();
        when(roleRepository.findByCode("ROLE_USER")).thenReturn(Optional.empty());

        authorityService.assignDefaultRole(userId);

        verify(userRoleRepository, never()).save(any(UserRole.class));
    }
}
