package com.festhub.concert_event_processor.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ArtistRegisteredEvent(
        String sourceEventId,
        UUID artistId,
        List<Concert> concerts) {

    public record Concert(
            String name,
            String description,
            Integer availableTickets,
            BigDecimal ticketPrice,
            Category category,
            Media media) {
    }

    public record Category(
            String name,
            String description) {
    }

    public record Media(
            String imageUrl) {
    }
}