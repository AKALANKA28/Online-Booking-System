package com.ticketing.seatservice.messaging;

import com.ticketing.seatservice.entity.Seat;
import com.ticketing.seatservice.entity.SeatStatus;
import com.ticketing.seatservice.repository.SeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventCancelledListenerTest {

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private EventCancelledListener eventCancelledListener;

    @Test
    void onEventCancelledMarksSeatsCancelled() {
        Seat seat = Seat.builder()
                .eventId(10L)
                .seatNumber("A1")
                .category("REGULAR")
                .price(new BigDecimal("45.00"))
                .status(SeatStatus.AVAILABLE)
                .createdAt(OffsetDateTime.now())
                .build();

        when(seatRepository.findByEventIdOrderBySeatNumberAsc(10L)).thenReturn(List.of(seat));

        eventCancelledListener.onEventCancelled(new EventCancelledMessage(10L, "Test Event", "Cancelled"));

        assertThat(seat.getStatus()).isEqualTo(SeatStatus.CANCELLED);
        assertThat(seat.getLockedByBookingReference()).isNull();
        assertThat(seat.getLockedUntil()).isNull();
        verify(seatRepository).saveAll(List.of(seat));
    }
}
