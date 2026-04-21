package com.ticketing.paymentnotificationservice.integration;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.ticketing.paymentnotificationservice.config.IntegrationProperties;
import com.ticketing.paymentnotificationservice.dto.PaymentProcessRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Stripe test-mode: confirms PaymentIntents using Stripe's <a href="https://stripe.com/docs/testing">instant test PaymentMethod IDs</a>
 * ({@code pm_card_visa}, etc.). Raw card numbers are not sent to the API — Stripe returns 402 unless you enable raw-card APIs on the account.
 */
@Component
@RequiredArgsConstructor
public class StripePaymentClient {

    /** Always succeeds in test mode when attached to a PaymentIntent. */
    private static final String PM_TEST_VISA_SUCCESS = "pm_card_visa";

    /** Charge is declined with a {@code card_declined} code in test mode. */
    private static final String PM_TEST_VISA_CHARGE_DECLINED = "pm_card_visa_chargeDeclined";

    private final IntegrationProperties integrationProperties;

    public PaymentIntent charge(PaymentProcessRequest request, String paymentReference) throws StripeException {
        long amountCents = toCents(request.amount());
        String currency = integrationProperties.getStripe().getCurrency();
        String pmId = resolveTestPaymentMethodId(request.cardToken());

        PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency(currency)
                .putMetadata("payment_reference", paymentReference)
                .putMetadata("booking_reference", request.bookingReference())
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(createParams);

        PaymentIntentConfirmParams confirmParams = PaymentIntentConfirmParams.builder()
                .setPaymentMethod(pmId)
                .build();

        return intent.confirm(confirmParams);
    }

    /**
     * Maps booking {@code cardToken} to Stripe test PaymentMethod IDs — never raw PANs.
     * <ul>
     *   <li>{@code pm_…} — passed through (your own test PaymentMethod id)</li>
     *   <li>{@code FAIL}, ends with {@code 0000}, or blank — decline test PM</li>
     *   <li>Any other value (including {@code 4242424242424242}) — success test PM</li>
     * </ul>
     */
    private String resolveTestPaymentMethodId(String cardToken) {
        if (cardToken != null && cardToken.startsWith("pm_")) {
            return cardToken;
        }
        if (shouldDecline(cardToken)) {
            return PM_TEST_VISA_CHARGE_DECLINED;
        }
        return PM_TEST_VISA_SUCCESS;
    }

    private boolean shouldDecline(String cardToken) {
        if (cardToken == null) {
            return true;
        }
        if (cardToken.equalsIgnoreCase("FAIL")) {
            return true;
        }
        return cardToken.endsWith("0000");
    }

    private static long toCents(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).longValueExact();
    }
}
