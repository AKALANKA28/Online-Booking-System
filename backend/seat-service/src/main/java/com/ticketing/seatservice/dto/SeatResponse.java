package com.ticketing.seatservice.dto;

import com.ticketing.seatservice.entity.Seat;
import com.ticketing.seatservice.entity.SeatStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SeatResponse(
        Long id,
        Long eventId,
        String seatNumber,
        String category,
        BigDecimal price,
        SeatStatus status,
        OffsetDateTime lockedUntil
) {
    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getEventId(),
                seat.getSeatNumber(),
                seat.getCategory(),
                seat.getPrice(),
                seat.getStatus(),
                seat.getLockedUntil()
        );
    }
}
