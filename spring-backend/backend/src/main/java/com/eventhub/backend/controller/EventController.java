package com.eventhub.backend.controller;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public EventResponse createEvent(
            @Valid @RequestBody EventRequest request) {

        return eventService.createEvent(request);
    }

    @GetMapping
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public EventResponse getEventById(
            @PathVariable Long id) {

        return eventService.getEventById(id);
    }

    @GetMapping("/my-events")
    public List<EventResponse> getMyEvents() {

        return eventService.getMyEvents();
    }

    @PutMapping("/{id}")
    public EventResponse updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request) {

        return eventService.updateEvent(id, request);
    }

    @DeleteMapping("/{id}")
    public String deleteEvent(
            @PathVariable Long id) {

        eventService.deleteEvent(id);

        return "Event deleted successfully";
    }
}
