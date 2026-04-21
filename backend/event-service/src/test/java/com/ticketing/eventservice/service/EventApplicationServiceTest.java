package com.ticketing.eventservice.service;

import com.ticketing.eventservice.dto.EventRequest;
import com.ticketing.eventservice.entity.Event;
import com.ticketing.eventservice.entity.EventStatus;
import com.ticketing.eventservice.messaging.EventMessagePublisher;
import com.ticketing.eventservice.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventApplicationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMessagePublisher eventMessagePublisher;

    @InjectMocks
    private EventApplicationService eventApplicationService;

    @Test
    void createPublishesEventCreated() {
        EventRequest request = new EventRequest(
                "Title",
                "Description",
                "Venue",
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(2),
                10,
                10,
                2,
                new BigDecimal("100.00"),
                new BigDecimal("50.00")
        );

        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Event event = eventApplicationService.create(request);

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventMessagePublisher).publishEventCreated(any(Event.class));
    }

    @Test
    void cancelPublishesEventCancelled() {
        Event event = Event.builder()
                .id(1L)
                .title("Test")
                .status(EventStatus.PUBLISHED)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Event cancelled = eventApplicationService.cancel(1L);

        assertThat(cancelled.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventMessagePublisher).publishEventCancelled(cancelled);
    }

    @Test
    void cancelThrowsIfAlreadyCancelled() {
        Event event = Event.builder()
                .id(2L)
                .title("Cancelled")
                .status(EventStatus.CANCELLED)
                .build();

        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));

        assertThrows(IllegalArgumentException.class, () -> eventApplicationService.cancel(2L));
    }
}
