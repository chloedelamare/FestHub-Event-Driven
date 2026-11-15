package com.festhub.publisher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DirectoryEventPublisher implements ApplicationRunner {

    private static final int EXPECTED_EVENT_COUNT = 1000;

    private final RabbitTemplate rabbitTemplate;
    private final Path eventDirectory;
    private final String exchange;
    private final String routingKey;

    public DirectoryEventPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${festhub.publisher.event-directory}") String eventDirectory,
            @Value("${festhub.publisher.exchange}") String exchange,
            @Value("${festhub.publisher.routing-key}") String routingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.eventDirectory = Path.of(eventDirectory);
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Path> eventFiles = findEventFiles();

        if (eventFiles.size() != EXPECTED_EVENT_COUNT) {
            throw new IllegalStateException(
                    "Expected " + EXPECTED_EVENT_COUNT
                    + " JSON files but found "
                    + eventFiles.size());
        }

        publishFiles(eventFiles);

        System.out.println(
                "Successfully published "
                + eventFiles.size()
                + " events.");
    }

    private List<Path> findEventFiles() throws IOException {
        if (!Files.isDirectory(eventDirectory)) {
            throw new IllegalStateException(
                    "Event directory does not exist: "
                    + eventDirectory);
        }

        try (Stream<Path> files = Files.list(eventDirectory)) {
            return files
                    .filter(Files::isRegularFile)
                    .filter(this::isJsonFile)
                    .sorted()
                    .toList();
        }
    }

    private boolean isJsonFile(Path file) {
        return file.getFileName()
                .toString()
                .toLowerCase()
                .endsWith(".json");
    }

    private void publishFiles(List<Path> eventFiles) {
        rabbitTemplate.invoke(operations -> {

            for (int index = 0; index < eventFiles.size(); index++) {
                Path file = eventFiles.get(index);

                try {
                    publishFile(file);

                    System.out.printf(
                            "Published %d/%d: %s%n",
                            index + 1,
                            eventFiles.size(),
                            file.getFileName());

                } catch (IOException exception) {
                    throw new IllegalStateException(
                            "Failed to read " + file,
                            exception);
                }
            }

            operations.waitForConfirmsOrDie(10_000);

            return null;
        });
    }

    private void publishFile(Path file) throws IOException {
        String json = Files.readString(
                file,
                StandardCharsets.UTF_8);

        Message message = MessageBuilder
                .withBody(json.getBytes(StandardCharsets.UTF_8))
                .setContentType("application/json")
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setMessageId(UUID.randomUUID().toString())
                .setHeader(
                        "sourceFile",
                        file.getFileName().toString())
                .build();

        rabbitTemplate.send(
                exchange,
                routingKey,
                message);
    }
}