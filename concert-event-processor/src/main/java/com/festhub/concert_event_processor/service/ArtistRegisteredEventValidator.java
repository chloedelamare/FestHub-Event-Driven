package com.festhub.concert_event_processor.service;

import com.festhub.concert_event_processor.model.ArtistRegisteredEvent;
import com.festhub.concert_event_processor.model.ArtistRegisteredEvent.Concert;

import org.springframework.stereotype.Component;

@Component
public class ArtistRegisteredEventValidator {
    public void validate(ArtistRegisteredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "ArtistRegistered event is required");
        }

        if (event.sourceEventId() == null
                || event.sourceEventId().isBlank()) {
            throw new IllegalArgumentException(
                    "Stable sourceEventId is required");
        }

        if (event.artistId() == null) {
            throw new IllegalArgumentException(
                    "Artist ID is required");
        }

        if (event.concerts() == null
                || event.concerts().isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one concert is required");
        }

        for (Concert concert : event.concerts()) {
            validateConcert(concert);
        }
    }

    private void validateConcert(Concert concert) {
        if (concert == null) {
            throw new IllegalArgumentException(
                    "Concert cannot be null");
        }

        if (concert.name() == null
                || concert.name().isBlank()
                || concert.name().length() > 2000) {
            throw new IllegalArgumentException(
                    "Concert name must contain 1–2000 characters");
        }

        if (concert.description() == null
                || concert.description().isBlank()
                || concert.description().length() > 10000) {
            throw new IllegalArgumentException(
                    "Description must contain 1–10000 characters");
        }

        if (concert.availableTickets() == null
                || concert.availableTickets() < 0) {
            throw new IllegalArgumentException(
                    "Available tickets must be nonnegative");
        }

        if (concert.ticketPrice() == null) {
            throw new IllegalArgumentException(
                    "Ticket price is required");
        }

        long cents;

        try {
            cents = concert.ticketPrice()
                    .movePointRight(2)
                    .longValueExact();

        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(
                    "Ticket price must fit in cents with at most two decimal places",
                    exception);
        }

        if (cents <= 0) {
            throw new IllegalArgumentException(
                    "Ticket price must be positive");
        }

        if (concert.availableTickets() == 0
                && cents > 10000) {
            throw new IllegalArgumentException(
                    "Sold-out ticket price cannot exceed $100");
        }
    }
}
