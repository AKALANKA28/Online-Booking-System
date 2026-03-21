package com.ticketing.seatservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ReleaseSeatsRequest(@NotBlank String bookingReference) {
}
