package com.festhub.concert_event_processor.messaging;

import com.festhub.concert_event_processor.model.ArtistRegisteredEvent;
import com.festhub.concert_event_processor.service.ArtistRegisteredEventValidator;
import com.festhub.concert_event_processor.service.ConcertEventProcessor;

import com.rabbitmq.client.Channel;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ArtistRegisteredListener {

    private static final Logger log = LoggerFactory.getLogger(ArtistRegisteredListener.class);

    private final ObjectMapper objectMapper;
    private final ArtistRegisteredEventValidator validator;
    private final ConcertEventProcessor processor;

    public ArtistRegisteredListener(
            ObjectMapper objectMapper,
            ArtistRegisteredEventValidator validator,
            ConcertEventProcessor processor) {

        this.objectMapper = objectMapper;
        this.validator = validator;
        this.processor = processor;
    }

    @RabbitListener(queues = "${festhub.concert.queue}")
    public void receive(Message message, Channel channel)
            throws IOException, InterruptedException {

        long deliveryTag =
                message.getMessageProperties().getDeliveryTag();

        try {
            ArtistRegisteredEvent event =
                    objectMapper.readValue(
                            message.getBody(),
                            ArtistRegisteredEvent.class);

            validator.validate(event);
            processor.process(event);

        } catch (JacksonException | IllegalArgumentException exception) {
            log.warn(
                    "Invalid concert message; routing to concert.dlq",
                    exception);

            channel.basicReject(deliveryTag, false);
            return;

        } catch (RuntimeException exception) {
            log.error(
                    "Processing failed; retrying message in five seconds",
                    exception);

            Thread.sleep(5000);

            channel.basicNack(deliveryTag, false, true);
            return;
        }

        channel.basicAck(deliveryTag, false);
    }
}