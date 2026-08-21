package com.gamifiedstudyhub.backend.activity;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/** Elasticsearch index for full-text search over the activity feed. */
public interface ActivitySearchRepository extends ElasticsearchRepository<ActivitySearchDocument, String> {

    /** Full-text match on the activity message (relevance-ranked). */
    List<ActivitySearchDocument> findByMessage(String message);
}
