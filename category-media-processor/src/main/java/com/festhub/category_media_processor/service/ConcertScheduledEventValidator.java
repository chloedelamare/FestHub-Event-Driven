package com.festhub.category_media_processor.service;

import com.festhub.category_media_processor.model.ConcertScheduledEvent;
import com.festhub.category_media_processor.model.ConcertScheduledEvent.Category;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class ConcertScheduledEventValidator {

    public void validate(ConcertScheduledEvent event) {
        if (event == null || !"ConcertScheduled".equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Expected a ConcertScheduled event");
        }
        if (event.schemaVersion() != 1) {
            throw new IllegalArgumentException(
                    "Unsupported ConcertScheduled schema version");
        }
        if (event.eventId() == null) {
            throw new IllegalArgumentException("Event ID is required");
        }
        if (event.data() == null || event.data().concertId() == null) {
            throw new IllegalArgumentException("Concert ID is required");
        }
        if (event.data().categories() == null
                || event.data().mediaUrls() == null) {
            throw new IllegalArgumentException(
                    "Categories and media URLs must be arrays");
        }

        Set<java.util.UUID> categoryIds = new HashSet<>();
        for (Category category : event.data().categories()) {
            if (category == null || category.id() == null) {
                throw new IllegalArgumentException(
                        "Every category must have an ID");
            }
            if (!categoryIds.add(category.id())) {
                throw new IllegalArgumentException(
                        "Category IDs must be unique within an event");
            }
            if (category.name() == null || category.name().isBlank()) {
                throw new IllegalArgumentException(
                        "Category name is required");
            }
            if (category.description() == null
                    || category.description().isBlank()) {
                throw new IllegalArgumentException(
                        "Category description is required");
            }
        }

        Set<String> mediaUrls = new HashSet<>();
        for (String mediaUrl : event.data().mediaUrls()) {
            validateMediaUrl(mediaUrl);
            if (!mediaUrls.add(mediaUrl)) {
                throw new IllegalArgumentException(
                        "Media URLs must be unique within an event");
            }
        }
    }

    private static void validateMediaUrl(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isBlank()) {
            throw new IllegalArgumentException("Media URL is required");
        }

        try {
            URI uri = URI.create(mediaUrl);
            String scheme = uri.getScheme();
            if (!uri.isAbsolute()
                    || uri.getHost() == null
                    || !("http".equalsIgnoreCase(scheme)
                            || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException(
                        "Media URL must be an absolute HTTP or HTTPS URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid promotional media URL: " + mediaUrl,
                    exception);
        }
    }
}
