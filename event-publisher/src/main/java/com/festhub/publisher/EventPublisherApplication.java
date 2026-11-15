package com.festhub.publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class EventPublisherApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(EventPublisherApplication.class, args);

        int exitCode = SpringApplication.exit(context);
        System.exit(exitCode);
    }
}