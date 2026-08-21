package com.gamifiedstudyhub.backend.activity;

import java.time.Instant;

/**
 * Kafka payload published when an activity is recorded. Consumed by
 * {@code ActivityEventConsumer} to keep the Elasticsearch index in sync with the
 * MongoDB source of truth.
 */
public record ActivityEvent(
        String id,
        String userId,
        String type,
        String message,
        Instant createdAt
) {
}
