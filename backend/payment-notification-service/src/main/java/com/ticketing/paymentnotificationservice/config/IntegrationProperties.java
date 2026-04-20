package com.ticketing.paymentnotificationservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.integrations")
public class IntegrationProperties {

    private final StripeProps stripe = new StripeProps();
    private final SendgridProps sendgrid = new SendgridProps();
    private final TwilioProps twilio = new TwilioProps();

    @Getter
    @Setter
    public static class StripeProps {
        /**
         * sk_test_... — when non-blank, real Stripe test-mode charges are used.
         */
        private String secretKey = "";
        private String webhookSecret = "";
        private String currency = "usd";
    }

    @Getter
    @Setter
    public static class SendgridProps {
        private String apiKey = "";
        /** Verified sender in SendGrid (single sender or domain). */
        private String fromEmail = "";
        private String fromName = "Ticketing";
    }

    @Getter
    @Setter
    public static class TwilioProps {
        private String accountSid = "";
        private String authToken = "";
        /** E.164, e.g. +15005550006 (magic test number in trial). */
        private String fromNumber = "";
    }

    public boolean stripeEnabled() {
        return stripe.getSecretKey() != null && !stripe.getSecretKey().isBlank();
    }

    public boolean sendgridEnabled() {
        return sendgrid.getApiKey() != null && !sendgrid.getApiKey().isBlank()
                && sendgrid.getFromEmail() != null && !sendgrid.getFromEmail().isBlank();
    }

    public boolean twilioSmsEnabled() {
        return twilio.getAccountSid() != null && !twilio.getAccountSid().isBlank()
                && twilio.getAuthToken() != null && !twilio.getAuthToken().isBlank()
                && twilio.getFromNumber() != null && !twilio.getFromNumber().isBlank();
    }
}
