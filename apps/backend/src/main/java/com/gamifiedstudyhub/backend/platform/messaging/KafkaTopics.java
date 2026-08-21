package com.gamifiedstudyhub.backend.platform.messaging;

/**
 * Central registry of Kafka topic names. Keep every topic string here (never
 * inline) so producers, consumers, and the {@code NewTopic} declarations in
 * {@link KafkaConfig} cannot drift apart.
 *
 * <p>Naming convention: {@code gsh.<domain>.<event>} — lower-kebab, dot-scoped.
 */
public final class KafkaTopics {

    /** Example/starter topic. Replace with real domain topics as features land. */
    public static final String STUDY_EVENTS = "gsh.study.events";

    private KafkaTopics() {
    }
}
