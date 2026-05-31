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
        private final UserRepository userRepository;
        private final EventRepository eventRepository;
        private final SessionRepository sessionRepository;

        public BookingServiceImpl(
                        BookingRepository bookingRepository,
                        UserRepository userRepository,
                        EventRepository eventRepository,
                        SessionRepository sessionRepository) {

                this.bookingRepository = bookingRepository;
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

                Event event = eventRepository.findById(request.getEventId())
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                Session session = sessionRepository.findById(request.getSessionId())
                                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

                // TODO: Implement booking logic with new structure
                // Calculate total amount from selected seats
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (BookingRequest.SelectedSeat seat : request.getSelectedSeats()) {
                        totalAmount = totalAmount.add(BigDecimal.valueOf(seat.getPrice()));
                }

                Booking booking = new Booking();
                booking.setUser(user);
                booking.setEvent(event);
                booking.setSession(session);
                booking.setTotalAmount(totalAmount);
                booking.setStatus(BookingStatus.CONFIRMED);

                // Map selected seats to BookedSeat entities
                List<Booking.BookedSeat> bookedSeats = request.getSelectedSeats().stream()
                                .map(seat -> new Booking.BookedSeat(seat.getSeatId(), seat.getSection(),
                                                BigDecimal.valueOf(seat.getPrice())))
                                .toList();
                booking.setSeats(bookedSeats);

                // Calculate ticketsSummary
                List<Booking.TicketSummary> ticketSummaries = request.getSelectedSeats().stream()
                                .collect(java.util.stream.Collectors.groupingBy(
                                                BookingRequest.SelectedSeat::getSection,
                                                java.util.stream.Collectors.collectingAndThen(
                                                                java.util.stream.Collectors.toList(),
                                                                list -> {
                                                                        Booking.TicketSummary summary = new Booking.TicketSummary();
                                                                        summary.setType(list.get(0).getSection());
                                                                        summary.setQuantity(list.size());
                                                                        summary.setTotalPrice(BigDecimal.valueOf(list
                                                                                        .stream()
                                                                                        .mapToDouble(BookingRequest.SelectedSeat::getPrice)
                                                                                        .sum()));
                                                                        return summary;
                                                                })))
                                .values()
                                .stream()
                                .toList();
                booking.setTicketsSummary(ticketSummaries);

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
        public BookingResponse cancelBooking(Long bookingId, String userEmail) {

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

                // TODO: Implement cancellation logic with new structure
                // Restore seats availability
                // Restore ticket availability

                booking.setStatus(BookingStatus.CANCELLED);

                Booking savedBooking = bookingRepository.save(booking);
                return mapToResponse(savedBooking);
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

                // TODO: Update repository query to use new Booking structure
                boolean booked = bookingRepository
                                .existsByUserAndEvent(
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

                // TODO: Implement cancellation logic with new structure
                // Restore seats availability
                // Restore ticket availability

                booking.setStatus(
                                BookingStatus.CANCELLED);

                Booking savedBooking = bookingRepository.save(booking);

                return mapToResponse(savedBooking);
        }

        private BookingResponse mapToResponse(Booking booking) {

                // Map seats
                List<BookingResponse.BookedSeat> bookedSeatsResponse = null;
                if (booking.getSeats() != null) {
                        bookedSeatsResponse = booking.getSeats().stream()
                                        .map(seat -> {
                                                BookingResponse.BookedSeat bookedSeat = new BookingResponse.BookedSeat();
                                                bookedSeat.setSeatId(seat.getSeatId());
                                                bookedSeat.setSection(seat.getSection());
                                                bookedSeat.setPrice(seat.getPrice());
                                                return bookedSeat;
                                        })
                                        .toList();
                }

                // Map ticketsSummary
                List<BookingResponse.TicketSummary> ticketSummariesResponse = null;
                if (booking.getTicketsSummary() != null) {
                        ticketSummariesResponse = booking.getTicketsSummary().stream()
                                        .map(summary -> {
                                                BookingResponse.TicketSummary ticketSummary = new BookingResponse.TicketSummary();
                                                ticketSummary.setType(summary.getType());
                                                ticketSummary.setQuantity(summary.getQuantity());
                                                ticketSummary.setTotalPrice(summary.getTotalPrice());
                                                return ticketSummary;
                                        })
                                        .toList();
                }

                return BookingResponse.builder()
                                ._id(booking.getId())
                                .id(booking.getId())
                                .user(booking.getUser() != null ? booking.getUser().getId() : null)
                                .event(booking.getEvent() != null ? booking.getEvent().getId() : null)
                                .session(booking.getSession() != null ? booking.getSession().getId() : null)
                                .seats(bookedSeatsResponse)
                                .ticketsSummary(ticketSummariesResponse)
                                .totalAmount(booking.getTotalAmount())
                                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                                .createdAt(booking.getCreatedAt())
                                .updatedAt(booking.getUpdatedAt())
                                .build();
        }
}