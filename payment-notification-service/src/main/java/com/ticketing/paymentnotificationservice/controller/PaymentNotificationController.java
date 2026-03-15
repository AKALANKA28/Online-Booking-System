package com.ticketing.paymentnotificationservice.controller;

import com.ticketing.paymentnotificationservice.dto.NotificationLogResponse;
import com.ticketing.paymentnotificationservice.dto.PaymentProcessRequest;
import com.ticketing.paymentnotificationservice.dto.PaymentProcessResponse;
import com.ticketing.paymentnotificationservice.dto.PaymentResponse;
import com.ticketing.paymentnotificationservice.service.NotificationApplicationService;
import com.ticketing.paymentnotificationservice.service.PaymentProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PaymentNotificationController {

    private final PaymentProcessingService paymentProcessingService;
    private final NotificationApplicationService notificationApplicationService;

    @PostMapping("/internal/payments/process")
    @Operation(summary = "Process payment for a booking")
    public PaymentProcessResponse processPayment(@Valid @RequestBody PaymentProcessRequest request) {
        return paymentProcessingService.processPayment(request);
    }

    @GetMapping("/api/payments/{paymentReference}")
    @Operation(summary = "Get a payment record by payment reference")
    public PaymentResponse getPayment(@PathVariable String paymentReference) {
        return PaymentResponse.from(paymentProcessingService.findByReference(paymentReference));
    }

    @GetMapping("/api/notifications/bookings/{bookingReference}")
    @Operation(summary = "Get notification logs by booking reference")
    public List<NotificationLogResponse> getNotificationLogs(@PathVariable String bookingReference) {
        return notificationApplicationService.findByBookingReference(bookingReference).stream()
                .map(NotificationLogResponse::from)
                .toList();
    }
}
