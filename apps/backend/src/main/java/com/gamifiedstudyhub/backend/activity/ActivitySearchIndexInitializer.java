package com.gamifiedstudyhub.backend.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;

/**
 * Creates the {@code gsh-activities} Elasticsearch index (with the mapping declared
 * on {@link ActivitySearchDocument}) once the app is ready, if it does not exist.
 *
 * <p>Because {@code ActivitySearchDocument} uses {@code createIndex = false}, index
 * creation is done here explicitly — this both keeps startup offline-safe when the
 * feature is off and ensures search never hits a missing index once it is on.
 */
@Component
@ConditionalOnProperty(prefix = "app.features.activity", name = "enabled", havingValue = "true")
public class ActivitySearchIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(ActivitySearchIndexInitializer.class);

    private final ElasticsearchOperations elasticsearchOperations;

    public ActivitySearchIndexInitializer(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureIndex() {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(ActivitySearchDocument.class);
            if (!indexOps.exists()) {
                indexOps.createWithMapping();
                log.info("Created Elasticsearch index 'gsh-activities'");
            }
        } catch (RuntimeException ex) {
            // Non-fatal: the app is already up. Log so the operator knows search is degraded.
            log.warn("Could not ensure Elasticsearch index 'gsh-activities' exists: {}", ex.getMessage());
        }
    }
}
