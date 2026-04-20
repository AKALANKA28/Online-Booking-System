package com.ticketing.userservice.config;

import com.ticketing.userservice.service.UserApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final UserApplicationService userApplicationService;

    @Bean
    public ApplicationRunner seedAdminUser(
            @Value("${app.bootstrap.admin.username}") String adminUsername,
            @Value("${app.bootstrap.admin.email}") String adminEmail,
            @Value("${app.bootstrap.admin.password}") String adminPassword
    ) {
        return args -> userApplicationService.ensureAdminUser(adminUsername, adminEmail, adminPassword);
    }
}
