package com.festhub.category_media_processor.service;

import com.festhub.category_media_processor.model.ConcertScheduledEvent;
import com.festhub.category_media_processor.model.ConcertScheduledEvent.Category;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConcertScheduledEventValidatorTest {

    private final ConcertScheduledEventValidator validator =
            new ConcertScheduledEventValidator();

    @Test
    void acceptsValidEventAndEmptyAssetLists() {
        ConcertScheduledEvent event = new ConcertScheduledEvent(
                "ConcertScheduled",
                1,
                UUID.randomUUID(),
                null,
                new ConcertScheduledEvent.Data(
                        UUID.randomUUID(),
                        List.of(),
                        List.of()));

        assertDoesNotThrow(() -> validator.validate(event));
    }

    @Test
    void rejectsWrongEventType() {
        ConcertScheduledEvent event = event(
                "ArtistRegistered",
                List.of(),
                List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(event));
    }

    @Test
    void rejectsRelativeOrNonHttpMediaUrls() {
        ConcertScheduledEvent event = event(
                "ConcertScheduled",
                List.of(),
                List.of("file:///poster.jpg"));

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(event));
    }

    @Test
    void rejectsCategoriesWithoutStableIds() {
        ConcertScheduledEvent event = event(
                "ConcertScheduled",
                List.of(new Category(null, "Pass", "Festival access")),
                List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(event));
    }

    private static ConcertScheduledEvent event(
            String eventType,
            List<Category> categories,
            List<String> mediaUrls) {
        return new ConcertScheduledEvent(
                eventType,
                1,
                UUID.randomUUID(),
                null,
                new ConcertScheduledEvent.Data(
                        UUID.randomUUID(),
                        categories,
                        mediaUrls));
    }
}
