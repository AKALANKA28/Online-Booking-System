package com.ticketing.paymentnotificationservice.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class StripeClientConfig {

    private final IntegrationProperties integrationProperties;

    @PostConstruct
    void init() {
        if (integrationProperties.stripeEnabled()) {
            Stripe.apiKey = integrationProperties.getStripe().getSecretKey();
        }
    }
}
