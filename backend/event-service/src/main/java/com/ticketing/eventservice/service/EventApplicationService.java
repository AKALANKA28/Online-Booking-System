package com.ticketing.eventservice.service;

import com.ticketing.eventservice.dto.EventRequest;
import com.ticketing.eventservice.entity.Event;
import com.ticketing.eventservice.entity.EventStatus;
import com.ticketing.eventservice.exception.NotFoundException;
import com.ticketing.eventservice.messaging.EventMessagePublisher;
import com.ticketing.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final EventMessagePublisher eventMessagePublisher;

    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
    }

    @Transactional
    public Event create(EventRequest request) {
        if (request.startsAt().isAfter(request.endsAt())) {
            throw new IllegalArgumentException("Event start time must be before end time");
        }
        if (request.vipRows() > request.totalRows()) {
            throw new IllegalArgumentException("VIP rows cannot exceed total rows");
        }

        Event event = Event.builder()
                .title(request.title())
                .description(request.description())
                .venue(request.venue())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .totalRows(request.totalRows())
                .seatsPerRow(request.seatsPerRow())
                .vipRows(request.vipRows())
                .vipPrice(request.vipPrice())
                .regularPrice(request.regularPrice())
                .status(EventStatus.PUBLISHED)
                .build();

        Event saved = eventRepository.save(event);
        eventMessagePublisher.publishEventCreated(saved);
        return saved;
    }

    @Transactional
    public Event updateStatus(Long eventId, EventStatus status) {
        Event event = findById(eventId);
        event.setStatus(status);
        return eventRepository.save(event);
    }
}
