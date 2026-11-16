package com.festhub.concert_event_processor.service;

import com.festhub.concert_event_processor.client.ConcertEventClient;
import com.festhub.concert_event_processor.messaging.ConcertScheduledPublisher;
import com.festhub.concert_event_processor.model.ArtistRegisteredEvent;
import com.festhub.concert_event_processor.model.ArtistRegisteredEvent.Concert;
import com.festhub.concert_event_processor.model.ConcertScheduledEvent;
import com.festhub.concert_event_processor.model.ConcertScheduledEvent.Category;
import com.festhub.concert_event_processor.model.ConcertScheduledEvent.Data;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class ConcertEventProcessor {

    private final ConcertEventClient concertClient;
    private final ConcertScheduledPublisher publisher;

    public ConcertEventProcessor(
            ConcertEventClient concertClient,
            ConcertScheduledPublisher publisher) {

        this.concertClient = concertClient;
        this.publisher = publisher;
    }

    public void process(ArtistRegisteredEvent event) {
        for (int index = 0;
                index < event.concerts().size();
                index++) {

            Concert concert = event.concerts().get(index);

            UUID concertId = stableId(
                    "concert:"
                            + event.sourceEventId().length()
                            + ":" + event.sourceEventId()
                            + ":" + index);

            concertClient.register(
                    concertId,
                    event.artistId(),
                    concert);

            List<Category> categories;

            if (concert.category() == null) {
                categories = Collections.singletonList(null);

            } else {
                UUID categoryId = stableId(
                        "category-for-concert:" + concertId);

                categories = List.of(new Category(
                        categoryId,
                        concert.category().name(),
                        concert.category().description()));
            }

            String imageUrl = concert.media() == null
                    ? null
                    : concert.media().imageUrl();

            List<String> mediaUrls =
                    Collections.singletonList(imageUrl);

            UUID messageId = stableId(
                    "ConcertScheduled:" + concertId);

            ConcertScheduledEvent output =
                    new ConcertScheduledEvent(
                            "ConcertScheduled",
                            1,
                            messageId,
                            Instant.now(),
                            new Data(
                                    concertId,
                                    categories,
                                    mediaUrls));

            publisher.publish(output);
        }
    }

    private static UUID stableId(String key) {
        return UUID.nameUUIDFromBytes(
                key.getBytes(StandardCharsets.UTF_8));
    }
}