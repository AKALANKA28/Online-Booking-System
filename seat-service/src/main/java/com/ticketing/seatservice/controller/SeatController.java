package com.ticketing.seatservice.controller;

import com.ticketing.seatservice.dto.*;
import com.ticketing.seatservice.service.SeatInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SeatController {

    private final SeatInventoryService seatInventoryService;

    @GetMapping("/api/seats/events/{eventId}")
    @Operation(summary = "Get all seats for an event")
    public List<SeatResponse> getSeatsForEvent(@PathVariable Long eventId) {
        return seatInventoryService.getSeatsForEvent(eventId).stream().map(SeatResponse::from).toList();
    }

    @PostMapping("/internal/seats/reserve")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reserve seats for a booking reference")
    public ReserveSeatsResponse reserveSeats(@Valid @RequestBody ReserveSeatsRequest request) {
        return seatInventoryService.reserveSeats(request);
    }

    @PostMapping("/internal/seats/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Confirm reserved seats")
    public void confirmSeats(@Valid @RequestBody ConfirmSeatsRequest request) {
        seatInventoryService.confirmSeats(request.bookingReference());
    }

    @PostMapping("/internal/seats/release")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Release reserved seats")
    public void releaseSeats(@Valid @RequestBody ReleaseSeatsRequest request) {
        seatInventoryService.releaseSeats(request.bookingReference());
    }
}
