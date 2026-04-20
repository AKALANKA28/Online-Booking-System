package com.ticketing.paymentnotificationservice.integration;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.ticketing.paymentnotificationservice.entity.Payment;
import com.ticketing.paymentnotificationservice.entity.PaymentStatus;
import com.ticketing.paymentnotificationservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public void handleEvent(Event event) {
        if (event.getType() == null || !event.getType().startsWith("payment_intent.")) {
            return;
        }
        StripeObject obj = event.getDataObjectDeserializer().getObject().orElse(null);
        if (!(obj instanceof PaymentIntent intent)) {
            return;
        }
        Optional<Payment> opt = paymentRepository.findByProviderReference(intent.getId());
        if (opt.isEmpty()) {
            log.warn("Stripe webhook: no local payment for intent {}", intent.getId());
            return;
        }
        Payment payment = opt.get();
        switch (event.getType()) {
            case "payment_intent.succeeded" -> {
                payment.setStatus(PaymentStatus.SUCCESS);
                log.info("Stripe webhook: marked payment {} SUCCESS", payment.getPaymentReference());
            }
            case "payment_intent.payment_failed", "payment_intent.canceled" -> {
                payment.setStatus(PaymentStatus.FAILED);
                log.info("Stripe webhook: marked payment {} FAILED", payment.getPaymentReference());
            }
            default -> { /* ignore */ }
        }
        paymentRepository.save(payment);
    }
}
