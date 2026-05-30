package com.eventhub.backend.service;

import com.eventhub.backend.dto.BookingRequest;
import com.eventhub.backend.dto.BookingResponse;
import com.eventhub.backend.dto.BookingStatusResponse;
import com.eventhub.backend.entity.Booking;

import java.util.List;

public interface BookingService {

        BookingResponse createBooking(
                        BookingRequest request);

        List<BookingResponse> getMyBookings();

        BookingResponse getBookingById(Long id);

        Booking cancelBooking(Long bookingId, String userEmail);

        BookingStatusResponse isEventBooked(
                        Long eventId,
                        String userEmail);

        BookingResponse cancelBooking(Long bookingId);
}