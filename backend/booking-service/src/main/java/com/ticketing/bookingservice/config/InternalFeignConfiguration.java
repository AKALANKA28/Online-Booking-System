package com.ticketing.bookingservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalFeignConfiguration {

    @Bean
    public RequestInterceptor internalTokenRequestInterceptor(@Value("${app.security.internal-api-key}") String internalApiKey) {
        return template -> template.header("X-Internal-Token", internalApiKey);
    }
}
