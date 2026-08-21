package com.gamifiedstudyhub.backend.platform.messaging;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares Kafka topics as {@link NewTopic} beans so Spring's {@code KafkaAdmin}
 * creates them on the broker at startup.
 *
 * <p>Guarded by {@code app.integrations.kafka.enabled} (default {@code false}) —
 * when messaging is off, no {@code KafkaAdmin} action runs and the broker is
 * never contacted.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.integrations.kafka", name = "enabled", havingValue = "true")
public class KafkaConfig {

    @Bean
    public NewTopic studyEventsTopic() {
        return TopicBuilder.name(KafkaTopics.STUDY_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
