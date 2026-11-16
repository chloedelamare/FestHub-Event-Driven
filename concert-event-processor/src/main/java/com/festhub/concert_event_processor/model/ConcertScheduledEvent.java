package com.festhub.concert_event_processor.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConcertScheduledEvent(
        String eventType,
        int schemaVersion,
        UUID eventId,
        Instant occurredAt,
        Data data) {

    public record Data(
            UUID concertId,
            List<Category> categories,
            List<String> mediaUrls) {
    }

    public record Category(
            UUID id,
            String name,
            String description) {
    }
}