package com.gamifiedstudyhub.backend.activity.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gamifiedstudyhub.backend.activity.ActivityEvent;
import com.gamifiedstudyhub.backend.activity.ActivitySearchDocument;
import com.gamifiedstudyhub.backend.activity.ActivitySearchRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ActivityEventConsumerTests {

    private final ActivitySearchRepository searchRepository = mock(ActivitySearchRepository.class);
    private final ActivityEventConsumer consumer = new ActivityEventConsumer(searchRepository);

    @Test
    void onActivityEvent_indexesEventIntoElasticsearch() {
        Instant now = Instant.now();
        ActivityEvent event = new ActivityEvent("id-1", "user-1", "QUIZ", "Finished the quiz", now);
        when(searchRepository.save(org.mockito.ArgumentMatchers.any(ActivitySearchDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        consumer.onActivityEvent(event);

        ArgumentCaptor<ActivitySearchDocument> captor = ArgumentCaptor.forClass(ActivitySearchDocument.class);
        verify(searchRepository).save(captor.capture());
        ActivitySearchDocument indexed = captor.getValue();

        assertEquals("id-1", indexed.getId());
        assertEquals("user-1", indexed.getUserId());
        assertEquals("QUIZ", indexed.getType());
        assertEquals("Finished the quiz", indexed.getMessage());
        assertEquals(now, indexed.getCreatedAt());
    }
}
