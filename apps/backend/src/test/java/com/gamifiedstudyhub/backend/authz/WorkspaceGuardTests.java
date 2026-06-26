package com.gamifiedstudyhub.backend.authz;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.user.entity.User;
import com.gamifiedstudyhub.backend.workspace.entity.WorkspaceMember;
import com.gamifiedstudyhub.backend.workspace.repository.WorkspaceMemberRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WorkspaceGuardTests {

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    private WorkspaceGuard workspaceGuard;

    private final UUID workspaceId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String... authorities) {
        workspaceGuard = new WorkspaceGuard(workspaceMemberRepository);
        User user = new User();
        ReflectionTestUtils.setField(user, "id", userId);
        CustomUserDetails principal = new CustomUserDetails(
                user, java.util.Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private WorkspaceMember member(String role, String status) {
        WorkspaceMember m = new WorkspaceMember();
        ReflectionTestUtils.setField(m, "role", role);
        ReflectionTestUtils.setField(m, "status", status);
        return m;
    }

    @Test
    void platformAdmin_bypassesWorkspaceCheck() {
        authenticateAs("ROLE_ADMIN");
        assertTrue(workspaceGuard.hasRole(workspaceId, "ADMIN"));
    }

    @Test
    void member_withMatchingActiveRole_isAllowed() {
        authenticateAs("ROLE_USER");
        when(workspaceMemberRepository.findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId))
                .thenReturn(Optional.of(member("ADMIN", "ACTIVE")));
        assertTrue(workspaceGuard.hasAnyRole(workspaceId, "OWNER", "ADMIN"));
    }

    @Test
    void member_withNonMatchingRole_isDenied() {
        authenticateAs("ROLE_USER");
        when(workspaceMemberRepository.findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId))
                .thenReturn(Optional.of(member("STUDENT", "ACTIVE")));
        assertFalse(workspaceGuard.hasRole(workspaceId, "ADMIN"));
    }

    @Test
    void nonMember_isDenied() {
        authenticateAs("ROLE_USER");
        when(workspaceMemberRepository.findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId))
                .thenReturn(Optional.empty());
        assertFalse(workspaceGuard.hasRole(workspaceId, "ADMIN"));
    }

    @Test
    void unauthenticated_isDenied() {
        workspaceGuard = new WorkspaceGuard(workspaceMemberRepository);
        lenient().when(workspaceMemberRepository.findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId))
                .thenReturn(Optional.of(member("ADMIN", "ACTIVE")));
        assertFalse(workspaceGuard.hasRole(workspaceId, "ADMIN"));
    }
}
