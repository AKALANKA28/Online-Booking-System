package com.ticketing.bookingservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReserveSeatsResponse(String bookingReference, Long eventId, BigDecimal totalAmount, List<ReservedSeatItem> seats) {
}
