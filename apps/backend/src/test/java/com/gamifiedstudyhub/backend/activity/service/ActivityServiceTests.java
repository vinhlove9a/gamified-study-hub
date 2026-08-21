package com.gamifiedstudyhub.backend.activity.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.activity.ActivityDocument;
import com.gamifiedstudyhub.backend.activity.ActivityEvent;
import com.gamifiedstudyhub.backend.activity.ActivityMongoRepository;
import com.gamifiedstudyhub.backend.activity.ActivitySearchDocument;
import com.gamifiedstudyhub.backend.activity.ActivitySearchRepository;
import com.gamifiedstudyhub.backend.activity.dto.ActivityRequest;
import com.gamifiedstudyhub.backend.activity.dto.ActivityResponse;
import com.gamifiedstudyhub.backend.platform.messaging.KafkaEventPublisher;
import com.gamifiedstudyhub.backend.platform.messaging.KafkaTopics;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;

class ActivityServiceTests {

    private final ActivityMongoRepository mongoRepository = mock(ActivityMongoRepository.class);
    private final ActivitySearchRepository searchRepository = mock(ActivitySearchRepository.class);

    @SuppressWarnings("unchecked")
    private ActivityService serviceWithPublisher(KafkaEventPublisher publisher) {
        ObjectProvider<KafkaEventPublisher> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(publisher);
        return new ActivityService(mongoRepository, searchRepository, provider);
    }

    @Test
    void record_persistsToMongoAndPublishesToKafka() {
        KafkaEventPublisher publisher = mock(KafkaEventPublisher.class);
        ActivityService service = serviceWithPublisher(publisher);
        UUID userId = UUID.randomUUID();
        // Persistence returns the same document it was given.
        when(mongoRepository.save(any(ActivityDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponse response = service.record(userId, new ActivityRequest("QUIZ", "Finished quiz"));

        verify(mongoRepository).save(any(ActivityDocument.class));

        ArgumentCaptor<ActivityEvent> eventCaptor = ArgumentCaptor.forClass(ActivityEvent.class);
        verify(publisher).publish(eq(KafkaTopics.STUDY_EVENTS), eq(userId.toString()), eventCaptor.capture());
        ActivityEvent event = eventCaptor.getValue();

        assertNotNull(response.id());
        assertEquals("QUIZ", response.type());
        assertEquals("Finished quiz", response.message());
        assertEquals(userId.toString(), response.userId());
        assertEquals(response.id(), event.id());
        assertEquals(userId.toString(), event.userId());
    }

    @Test
    void record_withoutKafka_stillPersistsAndDoesNotPublish() {
        ActivityService service = serviceWithPublisher(null); // messaging disabled
        UUID userId = UUID.randomUUID();
        when(mongoRepository.save(any(ActivityDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ActivityResponse response = service.record(userId, new ActivityRequest("NOTE", "Wrote a note"));

        assertNotNull(response.id());
        verify(mongoRepository).save(any(ActivityDocument.class));
        // No publisher bean → nothing to interact with; search side untouched.
        verifyNoInteractions(searchRepository);
    }

    @Test
    void search_queriesElasticsearchAndMaps() {
        ActivityService service = serviceWithPublisher(mock(KafkaEventPublisher.class));
        ActivitySearchDocument hit = new ActivitySearchDocument();
        hit.setId("id-1");
        hit.setUserId("user-1");
        hit.setType("QUIZ");
        hit.setMessage("calculus quiz");
        hit.setCreatedAt(Instant.now());
        when(searchRepository.findByMessage("calculus")).thenReturn(List.of(hit));

        List<ActivityResponse> results = service.search("calculus");

        assertEquals(1, results.size());
        assertEquals("id-1", results.get(0).id());
        assertEquals("calculus quiz", results.get(0).message());
    }

    @Test
    void list_readsFromMongoForUser() {
        ActivityService service = serviceWithPublisher(mock(KafkaEventPublisher.class));
        UUID userId = UUID.randomUUID();
        ActivityDocument doc = new ActivityDocument();
        doc.setId("id-9");
        doc.setUserId(userId.toString());
        doc.setType("FLASHCARD");
        doc.setMessage("reviewed cards");
        doc.setCreatedAt(Instant.now());
        when(mongoRepository.findByUserIdOrderByCreatedAtDesc(userId.toString())).thenReturn(List.of(doc));

        List<ActivityResponse> results = service.list(userId);

        assertEquals(1, results.size());
        assertEquals("id-9", results.get(0).id());
        verify(searchRepository, never()).findByMessage(any());
    }
}
