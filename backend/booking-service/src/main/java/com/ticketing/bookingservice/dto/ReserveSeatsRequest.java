package com.ticketing.bookingservice.dto;

import java.util.List;

public record ReserveSeatsRequest(Long eventId, String bookingReference, List<String> seatNumbers) {
}
