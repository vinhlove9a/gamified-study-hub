package com.gamifiedstudyhub.backend.authz;

import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.workspace.entity.WorkspaceMember;
import com.gamifiedstudyhub.backend.workspace.repository.WorkspaceMemberRepository;
import java.util.Arrays;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Workspace-scoped authorization, usable from method security, e.g.
 * {@code @PreAuthorize("@workspaceGuard.hasRole(#workspaceId, 'ADMIN')")}.
 * Platform admins (ROLE_ADMIN) pass every check.
 */
@Component("workspaceGuard")
public class WorkspaceGuard {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final WorkspaceMemberRepository workspaceMemberRepository;

    public WorkspaceGuard(WorkspaceMemberRepository workspaceMemberRepository) {
        this.workspaceMemberRepository = workspaceMemberRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasRole(UUID workspaceId, String role) {
        return hasAnyRole(workspaceId, role);
    }

    @Transactional(readOnly = true)
    public boolean hasAnyRole(UUID workspaceId, String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (workspaceId == null
                || authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return false;
        }

        // Platform administrators bypass workspace-level checks.
        boolean platformAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (platformAdmin) {
            return true;
        }

        return workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, principal.getUserId())
                .filter(member -> ACTIVE_STATUS.equals(member.getStatus()))
                .map(WorkspaceMember::getRole)
                .map(memberRole -> Arrays.asList(roles).contains(memberRole))
                .orElse(false);
    }
}
