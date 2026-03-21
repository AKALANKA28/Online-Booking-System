package com.ticketing.seatservice.repository;

import com.ticketing.seatservice.entity.Seat;
import com.ticketing.seatservice.entity.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByEventIdOrderBySeatNumberAsc(Long eventId);

    List<Seat> findByEventIdAndSeatNumberIn(Long eventId, Collection<String> seatNumbers);

    long countByEventId(Long eventId);

    List<Seat> findByLockedByBookingReference(String bookingReference);

    List<Seat> findByStatusAndLockedUntilBefore(SeatStatus status, OffsetDateTime time);
}
