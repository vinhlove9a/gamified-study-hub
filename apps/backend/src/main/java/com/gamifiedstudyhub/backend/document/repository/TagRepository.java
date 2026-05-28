package com.gamifiedstudyhub.backend.document.repository;

import com.gamifiedstudyhub.backend.document.entity.Tag;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByWorkspaceIdAndDeletedAtIsNullOrderByNameAsc(UUID workspaceId);

    List<Tag> findByIdInAndDeletedAtIsNull(Collection<UUID> ids);
}
