package com.ticketing.paymentnotificationservice;

import com.ticketing.paymentnotificationservice.config.IntegrationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(IntegrationProperties.class)
public class PaymentNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentNotificationServiceApplication.class, args);
    }
}
