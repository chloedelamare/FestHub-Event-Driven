package com.festhub.booking_category.service;

import com.festhub.booking_category.model.BookingCategory;
import com.festhub.booking_category.repository.BookingCategoryRepository;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingCategoryServiceTest {

    private final BookingCategoryRepository repository =
            mock(BookingCategoryRepository.class);
    private final BookingCategoryService service =
            new BookingCategoryService(repository);

    @Test
    void registerPreservesTheSuppliedId() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(BookingCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookingCategory registered =
                service.register(id, "Festival Pass", "Festival access");

        assertEquals(id, registered.getId());
        verify(repository).saveAndFlush(any(BookingCategory.class));
    }

    @Test
    void registerIsIdempotentForMatchingCategory() {
        UUID id = UUID.randomUUID();
        BookingCategory existing =
                new BookingCategory(id, "Festival Pass", "Festival access");
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        BookingCategory registered =
                service.register(id, "Festival Pass", "Festival access");

        assertEquals(existing, registered);
    }

    @Test
    void registerRejectsReusingAnIdForDifferentCategoryDetails() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(
                new BookingCategory(id, "Festival Pass", "Festival access")));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(id, "VIP", "Different category"));
    }
}
