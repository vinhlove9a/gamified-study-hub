package com.gamifiedstudyhub.backend.activity.dto;

import com.gamifiedstudyhub.backend.activity.ActivityDocument;
import com.gamifiedstudyhub.backend.activity.ActivitySearchDocument;
import java.time.Instant;

/** Activity as returned to API clients. */
public record ActivityResponse(
        String id,
        String userId,
        String type,
        String message,
        Instant createdAt
) {

    public static ActivityResponse from(ActivityDocument doc) {
        return new ActivityResponse(doc.getId(), doc.getUserId(), doc.getType(), doc.getMessage(), doc.getCreatedAt());
    }

    public static ActivityResponse from(ActivitySearchDocument doc) {
        return new ActivityResponse(doc.getId(), doc.getUserId(), doc.getType(), doc.getMessage(), doc.getCreatedAt());
    }
}
