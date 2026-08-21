package com.gamifiedstudyhub.backend.platform.messaging;

import java.util.concurrent.CompletableFuture;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

/**
 * Thin, reusable wrapper over {@link KafkaTemplate} for publishing domain events.
 * Inject this from any service instead of touching {@code KafkaTemplate} directly.
 *
 * <p>Only created when {@code app.integrations.kafka.enabled=true}; keep call
 * sites optional (e.g. {@code ObjectProvider<KafkaEventPublisher>}) so a service
 * still works with messaging turned off.
 */
@Component
@ConditionalOnProperty(prefix = "app.integrations.kafka", name = "enabled", havingValue = "true")
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes {@code payload} to {@code topic} keyed by {@code key} (the key
     * controls partitioning / ordering). Returns a future for the send result;
     * fire-and-forget callers may ignore it.
     */
    public CompletableFuture<SendResult<String, Object>> publish(String topic, String key, Object payload) {
        return kafkaTemplate.send(topic, key, payload);
    }
}
