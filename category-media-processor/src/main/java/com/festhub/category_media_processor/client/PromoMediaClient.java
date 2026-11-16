package com.festhub.category_media_processor.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PromoMediaClient {

    private final RestClient client;

    public PromoMediaClient(
            @Value("${services.media.url}") String baseUrl) {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public void register(
            UUID mediaId,
            UUID concertId,
            String mediaUrl) {

        client.put()
                .uri("/api/media/{id}/registration", mediaId)
                .body(new RegistrationRequest(concertId, mediaUrl))
                .retrieve()
                .toBodilessEntity();
    }

    private record RegistrationRequest(UUID eventId, String imageUrl) {
    }
}
