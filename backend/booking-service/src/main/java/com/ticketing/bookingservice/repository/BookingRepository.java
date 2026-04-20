package com.ticketing.bookingservice.repository;

import com.ticketing.bookingservice.entity.Booking;
import com.ticketing.bookingservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Booking> findByEventId(Long eventId);

    @Query("SELECT COUNT(bi) FROM Booking b JOIN b.items bi WHERE b.eventId = :eventId AND b.status = :status")
    long countSoldSeatsByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") BookingStatus status);
}
