package com.ticketing.bookingservice.messaging;

import java.math.BigDecimal;
import java.util.List;

public record BookingResultMessage(
        String bookingReference,
        Long eventId,
        String userId,
        String userEmail,
        String userPhone,
        BigDecimal totalAmount,
        String status,
        List<String> seatNumbers
) {
}
