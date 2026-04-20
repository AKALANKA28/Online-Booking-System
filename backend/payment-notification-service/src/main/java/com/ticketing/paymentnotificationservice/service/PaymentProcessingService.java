package com.ticketing.paymentnotificationservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.ticketing.paymentnotificationservice.config.IntegrationProperties;
import com.ticketing.paymentnotificationservice.dto.PaymentProcessRequest;
import com.ticketing.paymentnotificationservice.dto.PaymentProcessResponse;
import com.ticketing.paymentnotificationservice.entity.Payment;
import com.ticketing.paymentnotificationservice.entity.PaymentStatus;
import com.ticketing.paymentnotificationservice.exception.NotFoundException;
import com.ticketing.paymentnotificationservice.integration.StripePaymentClient;
import com.ticketing.paymentnotificationservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private final PaymentRepository paymentRepository;
    private final IntegrationProperties integrationProperties;
    private final StripePaymentClient stripePaymentClient;

    @Transactional
    public PaymentProcessResponse processPayment(PaymentProcessRequest request) {
        String paymentReference = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (integrationProperties.stripeEnabled()) {
            return processWithStripe(request, paymentReference);
        }
        return processSimulated(request, paymentReference);
    }

    private PaymentProcessResponse processWithStripe(PaymentProcessRequest request, String paymentReference) {
        try {
            PaymentIntent intent = stripePaymentClient.charge(request, paymentReference);
            boolean success = "succeeded".equals(intent.getStatus());
            Payment payment = Payment.builder()
                    .paymentReference(paymentReference)
                    .providerReference(intent.getId())
                    .bookingReference(request.bookingReference())
                    .amount(request.amount())
                    .paymentMethod(request.paymentMethod())
                    .status(success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                    .userEmail(request.userEmail())
                    .build();
            Payment saved = paymentRepository.save(payment);
            String msg = success
                    ? "Stripe payment succeeded (" + intent.getId() + ")"
                    : "Stripe payment not succeeded (status=" + intent.getStatus() + ")";
            return new PaymentProcessResponse(
                    saved.getPaymentReference(),
                    saved.getProviderReference(),
                    saved.getStatus().name(),
                    success,
                    msg);
        } catch (StripeException e) {
            Payment payment = Payment.builder()
                    .paymentReference(paymentReference)
                    .providerReference("STRIPE-ERROR")
                    .bookingReference(request.bookingReference())
                    .amount(request.amount())
                    .paymentMethod(request.paymentMethod())
                    .status(PaymentStatus.FAILED)
                    .userEmail(request.userEmail())
                    .build();
            Payment saved = paymentRepository.save(payment);
            return new PaymentProcessResponse(
                    saved.getPaymentReference(),
                    saved.getProviderReference(),
                    PaymentStatus.FAILED.name(),
                    false,
                    "Stripe error: " + e.getMessage());
        }
    }

    private PaymentProcessResponse processSimulated(PaymentProcessRequest request, String paymentReference) {
        boolean success = isPaymentSuccessful(request.cardToken());
        Payment payment = Payment.builder()
                .paymentReference(paymentReference)
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
                success ? "Payment approved (simulator)" : "Payment declined by simulator");
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
