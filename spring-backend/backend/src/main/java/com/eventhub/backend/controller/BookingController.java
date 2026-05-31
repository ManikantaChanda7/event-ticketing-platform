package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.BookingRequest;
import com.eventhub.backend.dto.BookingResponse;
import com.eventhub.backend.dto.BookingStatusResponse;
import com.eventhub.backend.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

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
    public ApiResponse<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return new ApiResponse<>(true, "Booking created successfully", response);
    }

    @GetMapping("/my-bookings")
    public ApiResponse<List<BookingResponse>> getMyBookings() {
        List<BookingResponse> response = bookingService.getMyBookings();
        return new ApiResponse<>(true, "My bookings fetched successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingResponse> getBookingById(
            @PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return new ApiResponse<>(true, "Booking fetched successfully", response);
    }

    @PutMapping("/{bookingId}/cancel")
    public ApiResponse<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        BookingResponse response = bookingService.cancelBooking(bookingId, authentication.getName());
        return new ApiResponse<>(true, "Booking cancelled successfully", response);
    }

    @GetMapping("/event/{eventId}/is-booked")
    public ApiResponse<BookingStatusResponse> isEventBooked(
            @PathVariable Long eventId,
            Authentication authentication) {
        BookingStatusResponse response = bookingService.isEventBooked(eventId, authentication.getName());
        return new ApiResponse<>(true, "Booking status fetched successfully", response);
    }
}