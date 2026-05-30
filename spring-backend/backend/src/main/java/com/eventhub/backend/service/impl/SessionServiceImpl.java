package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.SessionRequest;
import com.eventhub.backend.dto.SessionResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.Session;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.repository.SessionRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.SessionService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;

    public SessionServiceImpl(
            SessionRepository sessionRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            OrganizerRepository organizerRepository) {

        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.organizerRepository = organizerRepository;
    }

    @Override
    public SessionResponse createSession(SessionRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organizer organizer = organizerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.getOrganizer().getId()
                .equals(organizer.getId())) {

            throw new RuntimeException(
                    "You are not allowed to create sessions for this event");
        }

        Session session = new Session();

        session.setEvent(event);

        session.setSessionDate(
                LocalDate.parse(request.getSessionDate()));

        session.setStartTime(
                LocalTime.parse(request.getStartTime()));

        session.setEndTime(
                LocalTime.parse(request.getEndTime()));

        session.setCapacity(request.getCapacity());

        session.setAvailableSeats(request.getCapacity());

        session = sessionRepository.save(session);

        return mapToResponse(session);
    }

    @Override
    public List<SessionResponse> getAllSessions() {

        return sessionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public SessionResponse getSessionById(Long id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        return mapToResponse(session);
    }

    @Override
    public List<SessionResponse> getSessionsByEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return sessionRepository.findByEvent(event)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SessionResponse mapToResponse(Session session) {

        return SessionResponse.builder()
                .id(session.getId())
                .sessionDate(session.getSessionDate().toString())
                .startTime(session.getStartTime().toString())
                .endTime(session.getEndTime().toString())
                .capacity(session.getCapacity())
                .availableSeats(session.getAvailableSeats())
                .eventTitle(session.getEvent().getTitle())
                .build();
    }
}