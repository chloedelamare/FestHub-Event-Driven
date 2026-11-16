package com.festhub.category_media_processor.messaging;

import com.festhub.category_media_processor.model.ConcertScheduledEvent;
import com.festhub.category_media_processor.service.CategoryMediaProcessor;
import com.festhub.category_media_processor.service.ConcertScheduledEventValidator;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class ConcertScheduledListener {

    private final ObjectMapper objectMapper;
    private final ConcertScheduledEventValidator validator;
    private final CategoryMediaProcessor processor;

    public ConcertScheduledListener(
            ObjectMapper objectMapper,
            ConcertScheduledEventValidator validator,
            CategoryMediaProcessor processor) {
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.processor = processor;
    }

    @RabbitListener(
            queues = "${festhub.category-media.queue}")
    public void receive(Message message) {
        ConcertScheduledEvent event;
        try {
            event = objectMapper.readValue(
                    message.getBody(),
                    ConcertScheduledEvent.class);
            validator.validate(event);
        } catch (JacksonException | IllegalArgumentException exception) {
            throw new AmqpRejectAndDontRequeueException(
                    "Invalid ConcertScheduled message",
                    exception);
        }

        processor.process(event);
    }
}
