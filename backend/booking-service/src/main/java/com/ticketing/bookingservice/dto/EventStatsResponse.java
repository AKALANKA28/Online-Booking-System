package com.ticketing.bookingservice.dto;

public record EventStatsResponse(
        Long eventId,
        long soldSeats
) {
}