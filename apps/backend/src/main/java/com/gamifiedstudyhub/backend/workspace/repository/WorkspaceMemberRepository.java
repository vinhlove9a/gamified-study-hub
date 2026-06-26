package com.gamifiedstudyhub.backend.workspace.repository;

import com.gamifiedstudyhub.backend.workspace.entity.WorkspaceMember;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, UUID> {

    Optional<WorkspaceMember> findByWorkspaceIdAndUserIdAndDeletedAtIsNull(UUID workspaceId, UUID userId);
}
