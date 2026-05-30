package com.eventhub.backend.controller;

import com.eventhub.backend.dto.BookingRequest;
import com.eventhub.backend.dto.BookingResponse;
import com.eventhub.backend.dto.BookingStatusResponse;
import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(
            BookingService bookingService) {

        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequest request) {

        return bookingService.createBooking(request);
    }

    @GetMapping("/my-bookings")
    public List<BookingResponse> getMyBookings() {

        return bookingService.getMyBookings();
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingById(
            @PathVariable Long id) {

        return bookingService.getBookingById(id);
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {

        Booking booking = bookingService.cancelBooking(
                bookingId,
                authentication.getName());

        return ResponseEntity.ok(booking);
    }

    @GetMapping("/event/{eventId}/is-booked")
    public ResponseEntity<BookingStatusResponse> isEventBooked(
            @PathVariable Long eventId,
            Authentication authentication) {

        return ResponseEntity.ok(
                bookingService.isEventBooked(
                        eventId,
                        authentication.getName()));
    }
}