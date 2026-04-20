package com.ticketing.bookingservice.dto;

public record PaymentProcessResponse(
        String paymentReference,
        String providerReference,
        String status,
        boolean success,
        String message
) {
}
