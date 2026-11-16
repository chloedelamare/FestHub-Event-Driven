package com.festhub.concert_event.controller;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record UpdateConcertEventCategoriesRequest(
        @NotNull Set<@NotNull UUID> categoryIds) {
}
