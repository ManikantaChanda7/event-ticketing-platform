
package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.repository.VenueRepository;
import com.eventhub.backend.service.EventService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(
            EventRepository eventRepository,
            OrganizerRepository organizerRepository,
            VenueRepository venueRepository,
            UserRepository userRepository) {

        this.eventRepository = eventRepository;
        this.organizerRepository = organizerRepository;
        this.venueRepository = venueRepository;
        this.userRepository = userRepository;
    }

    @Override
    public EventResponse createEvent(EventRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organizer organizer = organizerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setBannerImage(request.getBannerImage());
        event.setOrganizer(organizer);
        event.setVenue(venue);

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            event.setStatus(EventStatus.valueOf(request.getStatus().toUpperCase()));
        }

        event = eventRepository.save(event);

        return mapToResponse(event);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EventResponse getEventById(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return mapToResponse(event);
    }

    @Override
    public List<EventResponse> getMyEvents() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organizer organizer = organizerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        return eventRepository.findByOrganizer(organizer)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public EventResponse updateEvent(
            Long id,
            EventRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organizer organizer = organizerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // Ownership Validation
        if (!event.getOrganizer().getId()
                .equals(organizer.getId())) {

            throw new RuntimeException(
                    "You are not allowed to update this event");
        }

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setBannerImage(request.getBannerImage());
        event.setVenue(venue);

        if (request.getStatus() != null
                && !request.getStatus().isBlank()) {

            event.setStatus(
                    EventStatus.valueOf(
                            request.getStatus().toUpperCase()));
        }

        event = eventRepository.save(event);

        return mapToResponse(event);
    }

    @Override
    public void deleteEvent(Long id) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organizer organizer = organizerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId()
                .equals(organizer.getId())) {

            throw new RuntimeException(
                    "You are not allowed to delete this event");
        }

        eventRepository.delete(event);
    }

    private EventResponse mapToResponse(Event event) {

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .category(event.getCategory())
                .bannerImage(event.getBannerImage())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .organizerName(event.getOrganizer().getOrganizationName())
                .venueName(event.getVenue().getName())
                .build();
    }
}
