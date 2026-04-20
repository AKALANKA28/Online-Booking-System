package com.ticketing.paymentnotificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentProcessRequest(
        @NotBlank String bookingReference,
        @NotBlank String userEmail,
        @NotNull BigDecimal amount,
        @NotBlank String paymentMethod,
        @NotBlank String cardToken
) {
}
