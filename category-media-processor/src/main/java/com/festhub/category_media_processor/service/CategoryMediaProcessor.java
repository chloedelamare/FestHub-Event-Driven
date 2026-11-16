package com.festhub.category_media_processor.service;

import com.festhub.category_media_processor.client.BookingCategoryClient;
import com.festhub.category_media_processor.client.ConcertEventClient;
import com.festhub.category_media_processor.client.PromoMediaClient;
import com.festhub.category_media_processor.model.ConcertScheduledEvent;
import com.festhub.category_media_processor.model.ConcertScheduledEvent.Category;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class CategoryMediaProcessor {

    private final BookingCategoryClient categoryClient;
    private final ConcertEventClient concertEventClient;
    private final PromoMediaClient mediaClient;

    public CategoryMediaProcessor(
            BookingCategoryClient categoryClient,
            ConcertEventClient concertEventClient,
            PromoMediaClient mediaClient) {
        this.categoryClient = categoryClient;
        this.concertEventClient = concertEventClient;
        this.mediaClient = mediaClient;
    }

    public void process(ConcertScheduledEvent event) {
        UUID concertId = event.data().concertId();

        for (Category category : event.data().categories()) {
            categoryClient.register(category);
        }

        var categoryIds = event.data().categories().stream()
                .map(Category::id)
                .collect(Collectors.toCollection(HashSet::new));
        concertEventClient.registerCategories(concertId, categoryIds);

        for (String mediaUrl : event.data().mediaUrls()) {
            mediaClient.register(
                    stableMediaId(concertId, mediaUrl),
                    concertId,
                    mediaUrl);
        }
    }

    static UUID stableMediaId(UUID concertId, String mediaUrl) {
        String key = concertId + "\n" + mediaUrl;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}
