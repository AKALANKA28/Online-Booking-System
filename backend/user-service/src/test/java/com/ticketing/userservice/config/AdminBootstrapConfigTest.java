package com.ticketing.userservice.config;

import com.ticketing.userservice.service.UserApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapConfigTest {

    @Mock
    private UserApplicationService userApplicationService;

    @InjectMocks
    private AdminBootstrapConfig adminBootstrapConfig;

    @Test
    void seedAdminUserCallsEnsureAdminUser() throws Exception {
        ApplicationRunner runner = adminBootstrapConfig.seedAdminUser(
                "admin",
                "admin@ticketing.local",
                "secret"
        );

        runner.run(new DefaultApplicationArguments(new String[]{}));

        verify(userApplicationService).ensureAdminUser("admin", "admin@ticketing.local", "secret");
    }
}
