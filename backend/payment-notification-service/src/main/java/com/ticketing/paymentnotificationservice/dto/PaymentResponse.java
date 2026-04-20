package com.ticketing.paymentnotificationservice.dto;

import com.ticketing.paymentnotificationservice.entity.Payment;
import com.ticketing.paymentnotificationservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        String paymentReference,
        String bookingReference,
        BigDecimal amount,
        String paymentMethod,
        String providerReference,
        PaymentStatus status,
        String userEmail,
        OffsetDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getPaymentReference(),
                payment.getBookingReference(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getProviderReference(),
                payment.getStatus(),
                payment.getUserEmail(),
                payment.getCreatedAt()
        );
    }
}
