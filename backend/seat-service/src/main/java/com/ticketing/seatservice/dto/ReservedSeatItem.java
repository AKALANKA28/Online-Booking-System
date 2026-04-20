package com.ticketing.seatservice.dto;

import java.math.BigDecimal;

public record ReservedSeatItem(String seatNumber, String category, BigDecimal price) {
}
