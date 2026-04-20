package com.ticketing.bookingservice.dto;

import java.math.BigDecimal;

public record ReservedSeatItem(String seatNumber, String category, BigDecimal price) {
}
