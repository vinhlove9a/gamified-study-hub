package com.gamifiedstudyhub.backend.activity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Study-activity record — the source of truth for the activity feed, stored in
 * MongoDB (write-heavy, flexible, no joins needed).
 *
 * <p>No {@code @Indexed} field on purpose: an index definition would make the
 * index creator dial MongoDB at startup, breaking boot while the feature is off.
 * Queries here are small per-user scans, which is fine for the feed.
 */
@Document(collection = "activities")
public class ActivityDocument {

    @Id
    private String id;

    private String userId;

    private String type;

    private String message;

    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
