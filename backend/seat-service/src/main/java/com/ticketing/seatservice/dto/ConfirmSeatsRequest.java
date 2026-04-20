package com.ticketing.seatservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmSeatsRequest(@NotBlank String bookingReference) {
}
