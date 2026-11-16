package com.festhub.concert_event_processor.service;

import com.festhub.concert_event_processor.client.ConcertEventClient;
import com.festhub.concert_event_processor.messaging.ConcertScheduledPublisher;
import com.festhub.concert_event_processor.model.ArtistRegisteredEvent;
import com.festhub.concert_event_processor.model.ArtistRegisteredEvent.Concert;
import com.festhub.concert_event_processor.model.ConcertScheduledEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ConcertEventProcessorTest {

    private final ConcertEventClient concertClient =
            mock(ConcertEventClient.class);
    private final ConcertScheduledPublisher publisher =
            mock(ConcertScheduledPublisher.class);
    private final ConcertEventProcessor processor =
            new ConcertEventProcessor(concertClient, publisher);

    @Test
    void publishesEmptyListsWhenConcertHasNoCategoryOrMedia() {
        ArtistRegisteredEvent event = new ArtistRegisteredEvent(
                "source-event-1",
                UUID.randomUUID(),
                List.of(new Concert(
                        "Summer show",
                        "An outdoor concert",
                        100,
                        new BigDecimal("25.00"),
                        null,
                        null)));

        processor.process(event);

        ArgumentCaptor<ConcertScheduledEvent> publishedEvent =
                ArgumentCaptor.forClass(ConcertScheduledEvent.class);
        verify(publisher).publish(publishedEvent.capture());

        assertTrue(publishedEvent.getValue().data().categories().isEmpty());
        assertTrue(publishedEvent.getValue().data().mediaUrls().isEmpty());
    }
}
