package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.TicketTypeRequest;
import com.eventhub.backend.dto.TicketTypeResponse;
import com.eventhub.backend.entity.*;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.*;
import com.eventhub.backend.service.TicketTypeService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;

    public TicketTypeServiceImpl(
            TicketTypeRepository ticketTypeRepository,
            SessionRepository sessionRepository,
            UserRepository userRepository,
            OrganizerRepository organizerRepository) {

        this.ticketTypeRepository = ticketTypeRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.organizerRepository = organizerRepository;
    }

    @Override
    public TicketTypeResponse createTicketType(
            TicketTypeRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organizer organizer = organizerRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        Session session = sessionRepository
                .findById(request.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        if (!session.getEvent()
                .getOrganizer()
                .getId()
                .equals(organizer.getId())) {

            throw new RuntimeException(
                    "You are not allowed to create ticket types for this session");
        }

        TicketType ticketType = new TicketType();

        ticketType.setName(request.getName());
        ticketType.setPrice(request.getPrice());
        ticketType.setQuantity(request.getQuantity());
        ticketType.setRemainingQuantity(request.getQuantity());
        ticketType.setSession(session);

        ticketType = ticketTypeRepository.save(ticketType);

        return mapToResponse(ticketType);
    }

    @Override
    public List<TicketTypeResponse> getAllTicketTypes() {

        return ticketTypeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TicketTypeResponse getTicketTypeById(Long id) {

        TicketType ticketType = ticketTypeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

        return mapToResponse(ticketType);
    }

    @Override
    public List<TicketTypeResponse> getTicketTypesBySession(
            Long sessionId) {

        Session session = sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        return ticketTypeRepository.findBySession(session)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TicketTypeResponse mapToResponse(
            TicketType ticketType) {

        return TicketTypeResponse.builder()
                .id(ticketType.getId())
                .name(ticketType.getName())
                .price(ticketType.getPrice())
                .quantity(ticketType.getQuantity())
                .remainingQuantity(ticketType.getRemainingQuantity())
                .sessionDate(
                        ticketType.getSession()
                                .getSessionDate()
                                .toString())
                .eventTitle(
                        ticketType.getSession()
                                .getEvent()
                                .getTitle())
                .build();
    }
}