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

                session.setDate(
                                LocalDate.parse(request.getSessionDate()));

                session.setStartTime(request.getStartTime());

                session.setEndTime(request.getEndTime());

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

                // Map tickets
                List<SessionResponse.Ticket> ticketResponses = null;
                if (session.getTickets() != null) {
                        ticketResponses = session.getTickets().stream()
                                        .map(ticket -> {
                                                SessionResponse.Ticket ticketResponse = new SessionResponse.Ticket();
                                                ticketResponse.setType(ticket.getType());
                                                ticketResponse.setPrice(ticket.getPrice());
                                                ticketResponse.setAvailable(ticket.getAvailable());
                                                ticketResponse.setTotalSeats(ticket.getTotalSeats());
                                                return ticketResponse;
                                        })
                                        .toList();
                }

                // Map seats
                List<SessionResponse.SeatInfo> seatInfos = null;
                if (session.getSeats() != null) {
                        seatInfos = session.getSeats().stream()
                                        .map(seat -> {
                                                SessionResponse.SeatInfo seatInfo = new SessionResponse.SeatInfo();
                                                seatInfo.setSeatId(seat.getSeatId());
                                                seatInfo.setSection(seat.getSection());
                                                // Convert enum to lowercase string to match frontend expectations
                                                String status = seat.getStatus() != null
                                                                ? seat.getStatus().name().toLowerCase()
                                                                : "available";
                                                // Map LOCKED to blocked for frontend
                                                if ("locked".equals(status)) {
                                                        status = "blocked";
                                                }
                                                seatInfo.setStatus(status);
                                                seatInfo.setUser(
                                                                seat.getUser() != null ? seat.getUser().getId() : null);
                                                return seatInfo;
                                        })
                                        .toList();
                }

                return SessionResponse.builder()
                                ._id(session.getId())
                                .id(session.getId())
                                .date(session.getDate())
                                .startTime(session.getStartTime())
                                .endTime(session.getEndTime())
                                .releaseDate(session.getReleaseDate())
                                .event(session.getEvent() != null ? session.getEvent().getId() : null)
                                .tickets(ticketResponses)
                                .seats(seatInfos)
                                .occupancy(session.getOccupancy())
                                .build();
        }
}