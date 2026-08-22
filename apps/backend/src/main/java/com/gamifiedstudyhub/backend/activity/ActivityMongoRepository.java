package com.gamifiedstudyhub.backend.activity;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/** MongoDB store for the activity feed (the write-side source of truth). */
public interface ActivityMongoRepository extends MongoRepository<ActivityDocument, String> {

    List<ActivityDocument> findByUserIdOrderByCreatedAtDesc(String userId);
}
