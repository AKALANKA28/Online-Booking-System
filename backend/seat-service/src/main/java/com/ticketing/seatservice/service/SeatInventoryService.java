package com.ticketing.seatservice.service;

import com.ticketing.seatservice.dto.*;
import com.ticketing.seatservice.entity.Seat;
import com.ticketing.seatservice.entity.SeatStatus;
import com.ticketing.seatservice.exception.ConflictException;
import com.ticketing.seatservice.messaging.EventCreatedMessage;
import com.ticketing.seatservice.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatInventoryService {

    private final SeatRepository seatRepository;

    @Value("${app.hold-duration-minutes}")
    private long holdDurationMinutes;

    @Transactional(readOnly = true)
    public List<Seat> getSeatsForEvent(Long eventId) {
        return seatRepository.findByEventIdOrderBySeatNumberAsc(eventId);
    }

    @Transactional
    public void createSeatsForEvent(EventCreatedMessage message) {
        if (seatRepository.countByEventId(message.eventId()) > 0) {
            return;
        }

        List<Seat> seats = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < message.totalRows(); rowIndex++) {
            String rowLabel = String.valueOf((char) ('A' + rowIndex));
            boolean vip = rowIndex < message.vipRows();
            for (int seatIndex = 1; seatIndex <= message.seatsPerRow(); seatIndex++) {
                seats.add(Seat.builder()
                        .eventId(message.eventId())
                        .seatNumber(rowLabel + seatIndex)
                        .category(vip ? "VIP" : "REGULAR")
                        .price(vip ? message.vipPrice() : message.regularPrice())
                        .status(SeatStatus.AVAILABLE)
                        .build());
            }
        }
        seatRepository.saveAll(seats);
    }

    @Transactional
    public ReserveSeatsResponse reserveSeats(ReserveSeatsRequest request) {
        List<String> requestedSeatNumbers = request.seatNumbers().stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .distinct()
                .toList();

        List<Seat> seats = seatRepository.findByEventIdAndSeatNumberIn(request.eventId(), requestedSeatNumbers);
        if (seats.size() != requestedSeatNumbers.size()) {
            Set<String> found = seats.stream().map(Seat::getSeatNumber).collect(Collectors.toSet());
            List<String> missing = requestedSeatNumbers.stream().filter(seat -> !found.contains(seat)).toList();
            throw new ConflictException("Seats not found for event: " + missing);
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.CANCELLED) {
                throw new ConflictException("Seat is not available because the event is cancelled: " + seat.getSeatNumber());
            }
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new ConflictException("Seat already booked: " + seat.getSeatNumber());
            }
            if (seat.getStatus() == SeatStatus.RESERVED && seat.getLockedUntil() != null && seat.getLockedUntil().isAfter(now)
                    && !request.bookingReference().equals(seat.getLockedByBookingReference())) {
                throw new ConflictException("Seat is currently locked: " + seat.getSeatNumber());
            }
        }

        OffsetDateTime lockedUntil = now.plusMinutes(holdDurationMinutes);
        List<ReservedSeatItem> reservedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.RESERVED);
            seat.setLockedByBookingReference(request.bookingReference());
            seat.setLockedUntil(lockedUntil);
            reservedItems.add(new ReservedSeatItem(seat.getSeatNumber(), seat.getCategory(), seat.getPrice()));
            total = total.add(seat.getPrice());
        }
        seatRepository.saveAll(seats);

        reservedItems.sort(Comparator.comparing(ReservedSeatItem::seatNumber));
        return new ReserveSeatsResponse(request.bookingReference(), request.eventId(), total, reservedItems);
    }

    @Transactional
    public void confirmSeats(String bookingReference) {
        List<Seat> seats = seatRepository.findByLockedByBookingReference(bookingReference);
        if (seats.isEmpty()) {
            throw new ConflictException("No reserved seats found for booking reference " + bookingReference);
        }

        OffsetDateTime now = OffsetDateTime.now();
        for (Seat seat : seats) {
            if (seat.getLockedUntil() != null && seat.getLockedUntil().isBefore(now)) {
                throw new ConflictException("Seat lock expired before confirmation for " + seat.getSeatNumber());
            }
            seat.setStatus(SeatStatus.BOOKED);
            seat.setLockedByBookingReference(null);
            seat.setLockedUntil(null);
        }
        seatRepository.saveAll(seats);
    }

    @Transactional
    public void releaseSeats(String bookingReference) {
        List<Seat> seats = seatRepository.findByLockedByBookingReference(bookingReference);
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedByBookingReference(null);
            seat.setLockedUntil(null);
        }
        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }
    }

    @Transactional
    public void releaseSeatsForEvent(Long eventId) {
        List<Seat> seats = seatRepository.findByEventId(eventId);
        for (Seat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED || seat.getStatus() == SeatStatus.RESERVED) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockedByBookingReference(null);
                seat.setLockedUntil(null);
            }
        }
        if (!seats.isEmpty()) {
            seatRepository.saveAll(seats);
        }
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        List<Seat> expiredSeats = seatRepository.findByStatusAndLockedUntilBefore(SeatStatus.RESERVED, OffsetDateTime.now());
        for (Seat seat : expiredSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedByBookingReference(null);
            seat.setLockedUntil(null);
        }
        if (!expiredSeats.isEmpty()) {
            seatRepository.saveAll(expiredSeats);
        }
    }
}
