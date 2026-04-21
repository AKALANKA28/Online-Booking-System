package com.ticketing.bookingservice.service;

import com.ticketing.bookingservice.client.PaymentServiceClient;
import com.ticketing.bookingservice.client.SeatServiceClient;
import com.ticketing.bookingservice.dto.*;
import com.ticketing.bookingservice.entity.Booking;
import com.ticketing.bookingservice.entity.BookingStatus;
import com.ticketing.bookingservice.messaging.BookingResultPublisher;
import com.ticketing.bookingservice.repository.BookingRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingApplicationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SeatServiceClient seatServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private BookingResultPublisher bookingResultPublisher;

    @InjectMocks
    private BookingApplicationService bookingApplicationService;

    @Test
    void createBookingSuccessConfirmsSeatsAndPublishesConfirmed() {
        CreateBookingRequest request = new CreateBookingRequest(1L, List.of("A1"), "CARD", "pm_visa");
        UserContext context = new UserContext("user-1", "user@example.com", "+123456789", "CUSTOMER");
        ReserveSeatsResponse reserveResponse = new ReserveSeatsResponse(
                "ref-1",
                1L,
                new BigDecimal("100.00"),
                List.of(new ReservedSeatItem("A1", "REGULAR", new BigDecimal("100.00")))
        );
        PaymentProcessResponse paymentResponse = new PaymentProcessResponse("PAY-123", "PROV-123", "OK", true, "approved");

        when(seatServiceClient.reserveSeats(any())).thenReturn(reserveResponse);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentServiceClient.processPayment(any())).thenReturn(paymentResponse);

        Booking booking = bookingApplicationService.createBooking(request, context);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getPaymentReference()).isEqualTo("PAY-123");
        verify(seatServiceClient).confirmSeats(any(ConfirmSeatsRequest.class));
        verify(bookingResultPublisher).publishConfirmed(any(Booking.class));
    }

    @Test
    void createBookingPaymentFailureReleasesSeatsAndPublishesFailed() {
        CreateBookingRequest request = new CreateBookingRequest(1L, List.of("A2"), "CARD", "pm_visa");
        UserContext context = new UserContext("user-2", "user2@example.com", "+123456790", "CUSTOMER");
        ReserveSeatsResponse reserveResponse = new ReserveSeatsResponse(
                "ref-2",
                1L,
                new BigDecimal("150.00"),
                List.of(new ReservedSeatItem("A2", "REGULAR", new BigDecimal("150.00")))
        );

        when(seatServiceClient.reserveSeats(any())).thenReturn(reserveResponse);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentServiceClient.processPayment(any())).thenThrow(new RuntimeException("payment failed"));

        try {
            bookingApplicationService.createBooking(request, context);
        } catch (RuntimeException expected) {
            assertThat(expected).hasMessageContaining("payment failed");
        }

        verify(seatServiceClient).releaseSeats(any(ReleaseSeatsRequest.class));
        verify(bookingResultPublisher).publishFailed(any(Booking.class));
    }
}
