package com.gamifiedstudyhub.backend.document.dto;

import com.gamifiedstudyhub.backend.document.entity.DocumentStatus;
import com.gamifiedstudyhub.backend.document.entity.DocumentVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record UpdateDocumentMetadataRequest(
        UUID categoryId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        DocumentVisibility visibility,
        DocumentStatus status,
        List<UUID> tagIds
) {
}
