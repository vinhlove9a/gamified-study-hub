package com.gamifiedstudyhub.backend.activity.messaging;

import com.gamifiedstudyhub.backend.activity.ActivityEvent;
import com.gamifiedstudyhub.backend.activity.ActivitySearchDocument;
import com.gamifiedstudyhub.backend.activity.ActivitySearchRepository;
import com.gamifiedstudyhub.backend.platform.messaging.KafkaTopics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Keeps the Elasticsearch activity index in sync with MongoDB by consuming
 * {@link ActivityEvent}s from Kafka and indexing them. The listener container only
 * starts when {@code spring.kafka.listener.auto-startup} (tied to
 * {@code APP_KAFKA_ENABLED}) is true, so the broker is never contacted otherwise.
 */
@Component
@ConditionalOnProperty(prefix = "app.features.activity", name = "enabled", havingValue = "true")
public class ActivityEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ActivityEventConsumer.class);

    private final ActivitySearchRepository searchRepository;

    public ActivityEventConsumer(ActivitySearchRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @KafkaListener(topics = KafkaTopics.STUDY_EVENTS, groupId = "${spring.kafka.consumer.group-id}")
    public void onActivityEvent(ActivityEvent event) {
        ActivitySearchDocument doc = new ActivitySearchDocument();
        doc.setId(event.id());
        doc.setUserId(event.userId());
        doc.setType(event.type());
        doc.setMessage(event.message());
        doc.setCreatedAt(event.createdAt());

        searchRepository.save(doc);
        log.debug("Indexed activity {} into Elasticsearch", event.id());
    }
}
