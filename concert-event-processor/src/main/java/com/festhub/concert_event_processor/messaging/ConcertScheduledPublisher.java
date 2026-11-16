package com.festhub.concert_event_processor.messaging;

import com.festhub.concert_event_processor.model.ConcertScheduledEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class ConcertScheduledPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchange;
    private final String routingKey;

    public ConcertScheduledPublisher(
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper,
            @Value("${festhub.concert.exchange}") String exchange,
            @Value("${festhub.concert.routing-key}") String routingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publish(ConcertScheduledEvent event) {
        Message message = MessageBuilder
                .withBody(objectMapper.writeValueAsBytes(event))
                .setContentType("application/json")
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(event.eventId().toString())
                .build();

        CorrelationData correlation =
                new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.send(
                exchange,
                routingKey,
                message,
                correlation);

        CorrelationData.Confirm confirmation;

        try {
            confirmation = correlation.getFuture()
                    .get(15, TimeUnit.SECONDS);

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "Interrupted while confirming publication",
                    exception);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not confirm ConcertScheduled publication",
                    exception);
        }

        if (!confirmation.isAck()
                || correlation.getReturned() != null) {

            throw new IllegalStateException(
                    "ConcertScheduled was rejected or unroutable");
        }
    }
}