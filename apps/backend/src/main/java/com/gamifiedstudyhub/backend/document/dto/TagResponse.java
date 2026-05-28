package com.gamifiedstudyhub.backend.document.dto;

import java.util.UUID;

public record TagResponse(
        UUID id,
        UUID workspaceId,
        String name,
        String slug
) {
}
