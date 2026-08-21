package com.gamifiedstudyhub.backend.activity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Search-side projection of an activity, indexed in Elasticsearch. This is a
 * derived copy (Mongo remains the source of truth) kept in sync via the Kafka
 * consumer, so it is safe to rebuild by replaying events.
 *
 * <p>{@code message} is analyzed text (relevance search); {@code userId}/{@code type}
 * are keywords (exact filter). {@code createIndex = false} keeps startup offline-safe;
 * the index is auto-created by Elasticsearch on the first document write.
 */
@Document(indexName = "gsh-activities", createIndex = false)
public class ActivitySearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String userId;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Text)
    private String message;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
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
