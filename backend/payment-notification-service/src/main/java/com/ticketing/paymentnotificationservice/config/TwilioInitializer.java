package com.ticketing.paymentnotificationservice.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TwilioInitializer {

    private final IntegrationProperties integrationProperties;

    @PostConstruct
    void init() {
        if (integrationProperties.twilioSmsEnabled()) {
            Twilio.init(
                    integrationProperties.getTwilio().getAccountSid(),
                    integrationProperties.getTwilio().getAuthToken());
        }
    }
}
