package com.festhub.category_media_processor.service;

import com.festhub.category_media_processor.client.BookingCategoryClient;
import com.festhub.category_media_processor.client.ConcertEventClient;
import com.festhub.category_media_processor.client.PromoMediaClient;
import com.festhub.category_media_processor.model.ConcertScheduledEvent;
import com.festhub.category_media_processor.model.ConcertScheduledEvent.Category;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CategoryMediaProcessorTest {

    private final BookingCategoryClient categoryClient =
            mock(BookingCategoryClient.class);
    private final ConcertEventClient concertEventClient =
            mock(ConcertEventClient.class);
    private final PromoMediaClient mediaClient =
            mock(PromoMediaClient.class);
    private final CategoryMediaProcessor processor =
            new CategoryMediaProcessor(
                    categoryClient,
                    concertEventClient,
                    mediaClient);

    @Test
    void registersCategoriesBeforeLinksAndMedia() {
        UUID concertId = UUID.randomUUID();
        Category category = new Category(
                UUID.randomUUID(),
                "Festival Pass",
                "Festival access");
        String url = "https://example.test/poster.jpg";
        ConcertScheduledEvent event = new ConcertScheduledEvent(
                "ConcertScheduled",
                1,
                UUID.randomUUID(),
                null,
                new ConcertScheduledEvent.Data(
                        concertId,
                        List.of(category),
                        List.of(url)));

        processor.process(event);

        InOrder calls = inOrder(
                categoryClient,
                concertEventClient,
                mediaClient);
        calls.verify(categoryClient).register(category);
        calls.verify(concertEventClient).registerCategories(
                concertId,
                Set.of(category.id()));
        calls.verify(mediaClient).register(
                CategoryMediaProcessor.stableMediaId(concertId, url),
                concertId,
                url);
    }

    @Test
    void clearsLinksWhenTheEventHasNoCategories() {
        UUID concertId = UUID.randomUUID();
        ConcertScheduledEvent event = new ConcertScheduledEvent(
                "ConcertScheduled",
                1,
                UUID.randomUUID(),
                null,
                new ConcertScheduledEvent.Data(
                        concertId,
                        List.of(),
                        List.of()));

        processor.process(event);

        verify(concertEventClient).registerCategories(concertId, Set.of());
    }
}
