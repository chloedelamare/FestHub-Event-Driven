package com.festhub.category_media_processor.client;

import com.festhub.category_media_processor.model.ConcertScheduledEvent.Category;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BookingCategoryClient {

    private final RestClient client;

    public BookingCategoryClient(
            @Value("${services.category.url}") String baseUrl) {
        this.client = createClient(baseUrl);
    }

    public void register(Category category) {
        client.put()
                .uri("/api/category/{id}/registration", category.id())
                .body(new RegistrationRequest(
                        category.name(),
                        category.description()))
                .retrieve()
                .toBodilessEntity();
    }

    private static RestClient createClient(String baseUrl) {
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    private record RegistrationRequest(String name, String description) {
    }
}
