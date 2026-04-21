package com.ticketing.bookingservice.dto;

import com.ticketing.bookingservice.entity.Booking;
import com.ticketing.bookingservice.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BookingResponse(
        String bookingReference,
        Long eventId,
        String userId,
        String userEmail,
        String userPhone,
        BigDecimal totalAmount,
        BookingStatus status,
        String paymentReference,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<BookingItemResponse> items
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getBookingReference(),
                booking.getEventId(),
                booking.getUserId(),
                booking.getUserEmail(),
                booking.getUserPhone(),
                booking.getTotalAmount(),
                booking.getStatus(),
                booking.getPaymentReference(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                booking.getItems().stream().map(BookingItemResponse::from).toList()
        );
    }
}
