package com.festhub.category_media_processor.client;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ConcertEventClient {

    private final RestClient client;

    public ConcertEventClient(
            @Value("${services.concert.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public void registerCategories(UUID concertId, Set<UUID> categoryIds) {
        client.put()
                .uri("/api/events/{id}/categories", concertId)
                .body(new CategoriesRequest(categoryIds))
                .retrieve()
                .toBodilessEntity();
    }

    private record CategoriesRequest(Set<UUID> categoryIds) {
    }
}
