package com.festhub.concert_event_processor.client;

import com.festhub.concert_event_processor.model.ArtistRegisteredEvent.Concert;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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

    public void register(
            UUID concertId,
            UUID artistId,
            Concert concert) {

        CreateRequest request = new CreateRequest(
                concertId,
                concert.name(),
                concert.description(),
                concert.availableTickets(),
                concert.ticketPrice()
                        .movePointRight(2)
                        .longValueExact(),
                Set.of(artistId),
                Set.of());

        ExistingConcert existing = find(concertId);

        if (existing != null) {
            verifySame(existing, request);
            return;
        }

        try {
            client.post()
                    .uri("/api/events")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();

            if (status == 400 || status == 409 || status == 422) {
                existing = find(concertId);

                if (existing != null) {
                    verifySame(existing, request);
                    return;
                }

                throw new IllegalArgumentException(
                        "Concert API rejected the record: "
                                + exception.getResponseBodyAsString(),
                        exception);
            }

            throw exception;
        }
    }

    private ExistingConcert find(UUID concertId) {
        try {
            ExistingConcert result = client.get()
                    .uri("/api/events/{id}", concertId)
                    .retrieve()
                    .body(ExistingConcert.class);

            if (result == null) {
                throw new IllegalStateException(
                        "Concert lookup returned an empty response");
            }

            return result;

        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                return null;
            }

            throw exception;
        }
    }

    private void verifySame(
            ExistingConcert existing,
            CreateRequest request) {

        boolean matches =
                Objects.equals(existing.id(), request.id())
                && Objects.equals(
                        existing.name(), request.name())
                && Objects.equals(
                        existing.description(), request.description())
                && Objects.equals(
                        existing.availableTickets(),
                        request.availableTickets())
                && Objects.equals(
                        existing.ticketPriceCents(),
                        request.ticketPriceCents())
                && Objects.equals(
                        existing.artistIds(), request.artistIds());

        if (!matches) {
            throw new IllegalArgumentException(
                    "Concert ID already exists with different data: "
                            + request.id());
        }

    }

    private record CreateRequest(
            UUID id,
            String name,
            String description,
            Integer availableTickets,
            Long ticketPriceCents,
            Set<UUID> artistIds,
            Set<UUID> categoryIds) {
    }

    public record ExistingConcert(
            UUID id,
            String name,
            String description,
            Integer availableTickets,
            Long ticketPriceCents,
            Set<UUID> artistIds,
            Set<UUID> categoryIds) {
    }
}