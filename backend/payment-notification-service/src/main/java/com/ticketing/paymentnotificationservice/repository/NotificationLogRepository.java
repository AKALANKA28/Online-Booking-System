package com.ticketing.paymentnotificationservice.repository;

import com.ticketing.paymentnotificationservice.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByBookingReferenceOrderByCreatedAtDesc(String bookingReference);
}
