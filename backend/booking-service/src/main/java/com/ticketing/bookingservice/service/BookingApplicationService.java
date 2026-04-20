package com.ticketing.bookingservice.service;

import com.ticketing.bookingservice.client.PaymentServiceClient;
import com.ticketing.bookingservice.client.SeatServiceClient;
import com.ticketing.bookingservice.dto.*;
import com.ticketing.bookingservice.entity.BookingStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingApplicationService {

    private final BookingRepository bookingRepository;
    private final SeatServiceClient seatServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final BookingResultPublisher bookingResultPublisher;

    // ... existing methods ...

    @Transactional(readOnly = true)
    public long getSoldSeatCount(Long eventId) {
        return bookingRepository.countSoldSeatsByEventIdAndStatus(eventId, BookingStatus.CONFIRMED);
    }

    @Transactional
    public Booking createBooking(CreateBookingRequest request, UserContext context) {
        String bookingReference = UUID.randomUUID().toString();

        ReserveSeatsResponse reserveResponse = seatServiceClient.reserveSeats(
                new ReserveSeatsRequest(request.eventId(), bookingReference, request.seatNumbers())
        );

        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .eventId(request.eventId())
                .userId(context.userId())
                .userEmail(context.userEmail())
                .totalAmount(reserveResponse.totalAmount())
                .status(BookingStatus.PENDING_PAYMENT)
                .build();

        for (ReservedSeatItem seat : reserveResponse.seats()) {
            booking.addItem(BookingItem.builder()
                    .seatNumber(seat.seatNumber())
                    .category(seat.category())
                    .price(seat.price())
                    .build());
        }

        bookingRepository.save(booking);

        PaymentProcessResponse paymentResponse;
        try {
            paymentResponse = paymentServiceClient.processPayment(new PaymentProcessRequest(
                    bookingReference,
                    context.userEmail(),
                    reserveResponse.totalAmount(),
                    request.paymentMethod(),
                    request.cardToken()
            ));
        } catch (RuntimeException ex) {
            seatServiceClient.releaseSeats(new ReleaseSeatsRequest(bookingReference));
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            bookingResultPublisher.publishFailed(booking);
            throw ex;
        }

        booking.setPaymentReference(paymentResponse.paymentReference());
        if (paymentResponse.success()) {
            seatServiceClient.confirmSeats(new ConfirmSeatsRequest(bookingReference));
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            bookingResultPublisher.publishConfirmed(booking);
        } else {
            seatServiceClient.releaseSeats(new ReleaseSeatsRequest(bookingReference));
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);
            bookingResultPublisher.publishFailed(booking);
        }

        return booking;
    }

    @Transactional(readOnly = true)
    public Booking findByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingReference));
    }

    @Transactional(readOnly = true)
    public List<Booking> findByUserId(String userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
