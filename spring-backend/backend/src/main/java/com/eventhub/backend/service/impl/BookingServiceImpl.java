package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.BookingRequest;
import com.eventhub.backend.dto.BookingResponse;
import com.eventhub.backend.dto.BookingStatusResponse;
import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.BookingStatus;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.SessionRepository;
import com.eventhub.backend.repository.TicketTypeRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.BookingService;

import jakarta.transaction.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

        private final BookingRepository bookingRepository;
        private final TicketTypeRepository ticketTypeRepository;
        private final UserRepository userRepository;
        private final EventRepository eventRepository;
        private final SessionRepository sessionRepository;

        public BookingServiceImpl(
                        BookingRepository bookingRepository,
                        TicketTypeRepository ticketTypeRepository,
                        UserRepository userRepository,
                        EventRepository eventRepository,
                        SessionRepository sessionRepository) {

                this.bookingRepository = bookingRepository;
                this.ticketTypeRepository = ticketTypeRepository;
                this.userRepository = userRepository;
                this.eventRepository = eventRepository;
                this.sessionRepository = sessionRepository;
        }

        @Override
        public BookingResponse createBooking(BookingRequest request) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                TicketType ticketType = ticketTypeRepository
                                .findById(request.getTicketTypeId())
                                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found"));

                if (ticketType.getRemainingQuantity() < request.getQuantity()) {
                        throw new RuntimeException("Not enough tickets available");
                }

                Session session = ticketType.getSession();

                if (session.getAvailableSeats() < request.getQuantity()) {
                        throw new RuntimeException("Not enough seats available");
                }

                BigDecimal totalAmount = ticketType.getPrice()
                                .multiply(BigDecimal.valueOf(request.getQuantity()));

                Booking booking = new Booking();
                booking.setUser(user);
                booking.setTicketType(ticketType);
                booking.setQuantity(request.getQuantity());
                booking.setTotalAmount(totalAmount);
                booking.setStatus(BookingStatus.CONFIRMED);

                ticketType.setRemainingQuantity(
                                ticketType.getRemainingQuantity() - request.getQuantity());

                session.setAvailableSeats(
                                session.getAvailableSeats() - request.getQuantity());

                booking = bookingRepository.save(booking);

                return mapToResponse(booking);
        }

        @Override
        public List<BookingResponse> getMyBookings() {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                return bookingRepository.findByUser(user)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public BookingResponse getBookingById(Long id) {

                Booking booking = bookingRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                return mapToResponse(booking);
        }

        @Override
        @Transactional
        public Booking cancelBooking(Long bookingId, String userEmail) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Booking booking = bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

                if (!booking.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("You can only cancel your own booking");
                }

                if (booking.getStatus() == BookingStatus.CANCELLED) {
                        throw new RuntimeException("Booking already cancelled");
                }

                TicketType ticketType = booking.getTicketType();
                Session session = ticketType.getSession();

                ticketType.setRemainingQuantity(
                                ticketType.getRemainingQuantity() + booking.getQuantity());

                session.setAvailableSeats(
                                session.getAvailableSeats() + booking.getQuantity());

                booking.setStatus(BookingStatus.CANCELLED);

                return bookingRepository.save(booking);
        }

        @Override
        public BookingStatusResponse isEventBooked(
                        Long eventId,
                        String userEmail) {

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found"));

                boolean booked = bookingRepository
                                .existsByUserAndTicketType_Session_Event(
                                                user,
                                                event);

                return new BookingStatusResponse(booked);
        }

        @Override
        @Transactional
        public BookingResponse cancelBooking(
                        Long bookingId) {

                Booking booking = bookingRepository
                                .findById(bookingId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Booking not found"));

                if (booking.getStatus() == BookingStatus.CANCELLED) {

                        throw new RuntimeException(
                                        "Booking already cancelled");
                }

                TicketType ticketType = booking.getTicketType();

                Session session = ticketType.getSession();

                Integer quantity = booking.getQuantity();

                ticketType.setRemainingQuantity(
                                ticketType.getRemainingQuantity()
                                                + quantity);

                session.setAvailableSeats(
                                session.getAvailableSeats()
                                                + quantity);

                booking.setStatus(
                                BookingStatus.CANCELLED);

                ticketTypeRepository.save(ticketType);
                sessionRepository.save(session);

                Booking savedBooking = bookingRepository.save(booking);

                return mapToResponse(savedBooking);
        }

        private BookingResponse mapToResponse(Booking booking) {

                return BookingResponse.builder()
                                .id(booking.getId())
                                .username(booking.getUser().getUsername())
                                .eventTitle(
                                                booking.getTicketType()
                                                                .getSession()
                                                                .getEvent()
                                                                .getTitle())
                                .ticketName(booking.getTicketType().getName())
                                .quantity(booking.getQuantity())
                                .totalAmount(booking.getTotalAmount())
                                .status(booking.getStatus().name())
                                .build();
        }
}