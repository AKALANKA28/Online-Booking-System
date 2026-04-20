package com.ticketing.bookingservice.dto;

import java.math.BigDecimal;

public record PaymentProcessRequest(
        String bookingReference,
        String userEmail,
        BigDecimal amount,
        String paymentMethod,
        String cardToken
) {
}
