package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.BookingRequest;
import com.eventhub.backend.dto.BookingResponse;
import com.eventhub.backend.entity.*;
import com.eventhub.backend.enums.BookingStatus;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.repository.TicketTypeRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.service.BookingService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            TicketTypeRepository ticketTypeRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.userRepository = userRepository;
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