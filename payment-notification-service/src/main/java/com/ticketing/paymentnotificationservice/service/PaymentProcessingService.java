package com.ticketing.paymentnotificationservice.service;

import com.ticketing.paymentnotificationservice.dto.PaymentProcessRequest;
import com.ticketing.paymentnotificationservice.dto.PaymentProcessResponse;
import com.ticketing.paymentnotificationservice.entity.Payment;
import com.ticketing.paymentnotificationservice.entity.PaymentStatus;
import com.ticketing.paymentnotificationservice.exception.NotFoundException;
import com.ticketing.paymentnotificationservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentProcessResponse processPayment(PaymentProcessRequest request) {
        boolean success = isPaymentSuccessful(request.cardToken());
        Payment payment = Payment.builder()
                .paymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .providerReference("SIM-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                .bookingReference(request.bookingReference())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod())
                .status(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .userEmail(request.userEmail())
                .build();

        Payment saved = paymentRepository.save(payment);
        return new PaymentProcessResponse(
                saved.getPaymentReference(),
                saved.getProviderReference(),
                saved.getStatus().name(),
                success,
                success ? "Payment approved" : "Payment declined by simulator"
        );
    }

    @Transactional(readOnly = true)
    public Payment findByReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentReference));
    }

    boolean isPaymentSuccessful(String cardToken) {
        return cardToken != null && !cardToken.equalsIgnoreCase("FAIL") && !cardToken.endsWith("0000");
    }
}
