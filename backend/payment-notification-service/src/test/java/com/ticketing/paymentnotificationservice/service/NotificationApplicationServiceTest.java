package com.ticketing.paymentnotificationservice.service;

import com.ticketing.paymentnotificationservice.config.IntegrationProperties;
import com.ticketing.paymentnotificationservice.entity.NotificationLog;
import com.ticketing.paymentnotificationservice.entity.NotificationStatus;
import com.ticketing.paymentnotificationservice.integration.NotificationDispatchService;
import com.ticketing.paymentnotificationservice.messaging.BookingResultMessage;
import com.ticketing.paymentnotificationservice.repository.NotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private NotificationDispatchService notificationDispatchService;

    private IntegrationProperties integrationProperties;

    @InjectMocks
    private NotificationApplicationService notificationApplicationService;

    @BeforeEach
    void setUp() {
        integrationProperties = new IntegrationProperties();
    }

    @Test
    void handleBookingResultSendsEmailWhenSendgridConfigured() throws Exception {
        integrationProperties.getSendgrid().setApiKey("key");
        integrationProperties.getSendgrid().setFromEmail("noreply@example.com");
        integrationProperties.getSendgrid().setFromName("Ticketing");

        NotificationApplicationService service = new NotificationApplicationService(
                notificationLogRepository,
                integrationProperties,
                notificationDispatchService
        );

        BookingResultMessage message = new BookingResultMessage(
                "ref-1",
                7L,
                "user-1",
                "user@example.com",
                null,
                new BigDecimal("250.00"),
                "CONFIRMED",
                List.of("A1")
        );

        when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handleBookingResult(message);

        verify(notificationDispatchService).sendEmail("user@example.com", "Booking Confirmed - ref-1", "Your booking is confirmed for event 7. Seats: A1. Total paid: 250.00");
        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(captor.getValue().getChannel()).isEqualTo("EMAIL");
    }

    @Test
    void handleBookingResultFailsWhenNoChannelsConfigured() throws Exception {
        NotificationApplicationService service = new NotificationApplicationService(
                notificationLogRepository,
                integrationProperties,
                notificationDispatchService
        );

        BookingResultMessage message = new BookingResultMessage(
                "ref-2",
                8L,
                "user-2",
                "user2@example.com",
                null,
                new BigDecimal("150.00"),
                "CONFIRMED",
                List.of("B2")
        );

        when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.handleBookingResult(message);

        verify(notificationDispatchService, never()).sendEmail(anyString(), anyString(), anyString());
        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(captor.getValue().getChannel()).isEqualTo("NONE");
    }
}
