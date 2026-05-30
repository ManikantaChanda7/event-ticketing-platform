package com.eventhub.backend.service;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;

import java.util.List;

public interface EventService {

    EventResponse createEvent(EventRequest request);

    List<EventResponse> getAllEvents();

    EventResponse getEventById(Long id);

    List<EventResponse> getMyEvents();

    EventResponse updateEvent(Long id, EventRequest request);

    void deleteEvent(Long id);
}