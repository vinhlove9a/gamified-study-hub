package com.gamifiedstudyhub.backend.activity.service;

import com.gamifiedstudyhub.backend.activity.ActivityDocument;
import com.gamifiedstudyhub.backend.activity.ActivityEvent;
import com.gamifiedstudyhub.backend.activity.ActivityMongoRepository;
import com.gamifiedstudyhub.backend.activity.ActivitySearchRepository;
import com.gamifiedstudyhub.backend.activity.dto.ActivityRequest;
import com.gamifiedstudyhub.backend.activity.dto.ActivityResponse;
import com.gamifiedstudyhub.backend.platform.messaging.KafkaEventPublisher;
import com.gamifiedstudyhub.backend.platform.messaging.KafkaTopics;
import io.micrometer.observation.annotation.Observed;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Reference feature wiring MongoDB + Kafka + Elasticsearch together:
 * <ol>
 *   <li>{@link #record} writes the activity to MongoDB (source of truth) and
 *       publishes an {@link ActivityEvent} to Kafka.</li>
 *   <li>{@code ActivityEventConsumer} indexes it into Elasticsearch.</li>
 *   <li>{@link #search} queries Elasticsearch; {@link #list} reads MongoDB.</li>
 * </ol>
 *
 * <p>Only active when {@code app.features.activity.enabled=true}. Kafka publishing
 * degrades gracefully (via {@link ObjectProvider}) when messaging is disabled — the
 * MongoDB write still succeeds. Methods are {@code @Observed} so they emit a
 * Micrometer timer and a trace span.
 */
@Service
@ConditionalOnProperty(prefix = "app.features.activity", name = "enabled", havingValue = "true")
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final ActivityMongoRepository mongoRepository;
    private final ActivitySearchRepository searchRepository;
    private final ObjectProvider<KafkaEventPublisher> kafkaPublisher;

    public ActivityService(
            ActivityMongoRepository mongoRepository,
            ActivitySearchRepository searchRepository,
            ObjectProvider<KafkaEventPublisher> kafkaPublisher
    ) {
        this.mongoRepository = mongoRepository;
        this.searchRepository = searchRepository;
        this.kafkaPublisher = kafkaPublisher;
    }

    @Observed(name = "activity.record")
    public ActivityResponse record(UUID userId, ActivityRequest request) {
        ActivityDocument doc = new ActivityDocument();
        doc.setId(UUID.randomUUID().toString());
        doc.setUserId(userId.toString());
        doc.setType(request.type());
        doc.setMessage(request.message());
        doc.setCreatedAt(Instant.now());

        ActivityDocument saved = mongoRepository.save(doc);

        KafkaEventPublisher publisher = kafkaPublisher.getIfAvailable();
        if (publisher != null) {
            ActivityEvent event = new ActivityEvent(
                    saved.getId(), saved.getUserId(), saved.getType(), saved.getMessage(), saved.getCreatedAt());
            publisher.publish(KafkaTopics.STUDY_EVENTS, saved.getUserId(), event);
        } else {
            log.warn("Kafka disabled — activity {} saved to MongoDB but not published for indexing", saved.getId());
        }

        return ActivityResponse.from(saved);
    }

    public List<ActivityResponse> list(UUID userId) {
        return mongoRepository.findByUserIdOrderByCreatedAtDesc(userId.toString())
                .stream()
                .map(ActivityResponse::from)
                .toList();
    }

    @Observed(name = "activity.search")
    public List<ActivityResponse> search(String query) {
        return searchRepository.findByMessage(query)
                .stream()
                .map(ActivityResponse::from)
                .toList();
    }
}
