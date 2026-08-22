package com.gamifiedstudyhub.backend.authz;

import com.gamifiedstudyhub.backend.auth.security.CustomUserDetails;
import com.gamifiedstudyhub.backend.workspace.entity.WorkspaceMember;
import com.gamifiedstudyhub.backend.workspace.repository.WorkspaceMemberRepository;
import java.util.Arrays;
import java.util.Optional;
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
        CustomUserDetails principal = currentPrincipal();
        if (workspaceId == null || principal == null) {
            return false;
        }
        // Platform administrators bypass workspace-level checks.
        if (isPlatformAdmin()) {
            return true;
        }
        return activeMember(workspaceId, principal.getUserId())
                .map(WorkspaceMember::getRole)
                .map(memberRole -> Arrays.asList(roles).contains(memberRole))
                .orElse(false);
    }

    /**
     * True when the caller is an active member of the workspace, regardless of role.
     * Use as the read baseline (e.g. {@code @PreAuthorize("@workspaceGuard.isMember(#workspaceId)")}),
     * where role-level granularity is not required. Platform admins pass.
     */
    @Transactional(readOnly = true)
    public boolean isMember(UUID workspaceId) {
        CustomUserDetails principal = currentPrincipal();
        if (workspaceId == null || principal == null) {
            return false;
        }
        if (isPlatformAdmin()) {
            return true;
        }
        return activeMember(workspaceId, principal.getUserId()).isPresent();
    }

    private CustomUserDetails currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails principal)) {
            return null;
        }
        return principal;
    }

    private boolean isPlatformAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private Optional<WorkspaceMember> activeMember(UUID workspaceId, UUID userId) {
        return workspaceMemberRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .filter(member -> ACTIVE_STATUS.equals(member.getStatus()));
    }
}
