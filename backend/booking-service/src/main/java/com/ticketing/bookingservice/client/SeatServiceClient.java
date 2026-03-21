package com.ticketing.bookingservice.client;

import com.ticketing.bookingservice.config.InternalFeignConfiguration;
import com.ticketing.bookingservice.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "seat-service", url = "${services.seat.base-url}", configuration = InternalFeignConfiguration.class)
public interface SeatServiceClient {

    @PostMapping("/internal/seats/reserve")
    ReserveSeatsResponse reserveSeats(@RequestBody ReserveSeatsRequest request);

    @PostMapping("/internal/seats/confirm")
    void confirmSeats(@RequestBody ConfirmSeatsRequest request);

    @PostMapping("/internal/seats/release")
    void releaseSeats(@RequestBody ReleaseSeatsRequest request);
}
