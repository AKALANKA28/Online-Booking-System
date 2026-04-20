package com.ticketing.bookingservice.controller;

import com.ticketing.bookingservice.dto.BookingResponse;
import com.ticketing.bookingservice.dto.CreateBookingRequest;
import com.ticketing.bookingservice.dto.UserContext;
import com.ticketing.bookingservice.service.BookingApplicationService;
import com.ticketing.bookingservice.util.UserContextGuards;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingApplicationService bookingApplicationService;

    @PostMapping
    @Operation(summary = "Create a booking for the authenticated user")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Phone", required = false) String userPhone,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CreateBookingRequest request) {

        UserContext context = UserContextGuards.requireAuthenticated(userId, userEmail, userPhone, role);
        BookingResponse response = BookingResponse.from(bookingApplicationService.createBooking(request, context));
        HttpStatus status = response.status().name().equals("FAILED") ? HttpStatus.CONFLICT : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{bookingReference}")
    @Operation(summary = "Get booking details by booking reference")
    public BookingResponse findByReference(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Phone", required = false) String userPhone,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable String bookingReference) {

        UserContext context = UserContextGuards.requireAuthenticated(userId, userEmail, userPhone, role);
        var booking = bookingApplicationService.findByReference(bookingReference);
        UserContextGuards.requireOwnerOrAdmin(context, booking.getUserId());
        return BookingResponse.from(booking);
    }

    @GetMapping("/me")
    @Operation(summary = "Get bookings for the authenticated user")
    public List<BookingResponse> findMine(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String userEmail,
            @RequestHeader(value = "X-User-Phone", required = false) String userPhone,
            @RequestHeader(value = "X-User-Role", required = false) String role) {

        UserContext context = UserContextGuards.requireAuthenticated(userId, userEmail, userPhone, role);
        return bookingApplicationService.findByUserId(context.userId()).stream().map(BookingResponse::from).toList();
    }
}
