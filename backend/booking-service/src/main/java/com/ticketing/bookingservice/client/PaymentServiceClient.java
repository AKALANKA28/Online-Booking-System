package com.ticketing.bookingservice.client;

import com.ticketing.bookingservice.config.InternalFeignConfiguration;
import com.ticketing.bookingservice.dto.PaymentProcessRequest;
import com.ticketing.bookingservice.dto.PaymentProcessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${services.payment.base-url}", configuration = InternalFeignConfiguration.class)
public interface PaymentServiceClient {

    @PostMapping("/internal/payments/process")
    PaymentProcessResponse processPayment(@RequestBody PaymentProcessRequest request);
}
