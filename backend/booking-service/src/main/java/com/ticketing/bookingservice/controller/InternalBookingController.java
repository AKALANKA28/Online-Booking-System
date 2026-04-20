package com.ticketing.bookingservice.controller;

import com.ticketing.bookingservice.dto.EventStatsResponse;
import com.ticketing.bookingservice.service.BookingApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/bookings")
@RequiredArgsConstructor
public class InternalBookingController {

    private final BookingApplicationService bookingApplicationService;

    @GetMapping("/events/{eventId}/stats")
    @Operation(summary = "Get sold seat count for an event (internal)")
    public EventStatsResponse getEventStats(@PathVariable Long eventId) {
        long soldSeats = bookingApplicationService.getSoldSeatCount(eventId);
        return new EventStatsResponse(eventId, soldSeats);
    }
}