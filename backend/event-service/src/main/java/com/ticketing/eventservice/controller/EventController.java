package com.ticketing.eventservice.controller;

import com.ticketing.eventservice.dto.EventRequest;
import com.ticketing.eventservice.dto.EventResponse;
import com.ticketing.eventservice.dto.EventStatusUpdateRequest;
import com.ticketing.eventservice.service.EventApplicationService;
import com.ticketing.eventservice.util.GatewaySecurityGuards;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventApplicationService eventApplicationService;

    @GetMapping
    @Operation(summary = "List all events")
    public List<EventResponse> findAll() {
        return eventApplicationService.findAll().stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get an event by id")
    public EventResponse findById(@PathVariable Long eventId) {
        return EventResponse.from(eventApplicationService.findById(eventId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an event (ADMIN only)")
    public EventResponse create(@RequestHeader(value = "X-User-Role", required = false) String role,
                                @Valid @RequestBody EventRequest request) {
        GatewaySecurityGuards.requireAdmin(role);
        return EventResponse.from(eventApplicationService.create(request));
    }

    @PutMapping("/{eventId}/status")
    @Operation(summary = "Update event status (ADMIN only)")
    public EventResponse updateStatus(@RequestHeader(value = "X-User-Role", required = false) String role,
                                      @PathVariable Long eventId,
                                      @Valid @RequestBody EventStatusUpdateRequest request) {
        GatewaySecurityGuards.requireAdmin(role);
        return EventResponse.from(eventApplicationService.updateStatus(eventId, request.status()));
    }
}
