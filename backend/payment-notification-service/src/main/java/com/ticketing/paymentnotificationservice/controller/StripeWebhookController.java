package com.ticketing.paymentnotificationservice.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.ticketing.paymentnotificationservice.config.IntegrationProperties;
import com.ticketing.paymentnotificationservice.integration.StripeWebhookService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Hidden
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final IntegrationProperties integrationProperties;
    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripe(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader) {

        if (!integrationProperties.stripeEnabled()
                || integrationProperties.getStripe().getWebhookSecret() == null
                || integrationProperties.getStripe().getWebhookSecret().isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Stripe webhook not configured");
        }
        if (sigHeader == null || sigHeader.isBlank()) {
            return ResponseEntity.badRequest().body("Missing Stripe-Signature");
        }
        try {
            Event event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    integrationProperties.getStripe().getWebhookSecret());
            stripeWebhookService.handleEvent(event);
            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }
    }
}
