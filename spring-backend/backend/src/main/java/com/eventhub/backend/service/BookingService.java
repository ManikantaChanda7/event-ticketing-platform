package com.eventhub.backend.service;

import com.eventhub.backend.dto.BookingRequest;
import com.eventhub.backend.dto.BookingResponse;
import com.eventhub.backend.dto.BookingStatusResponse;

import java.util.List;

public interface BookingService {

        BookingResponse createBooking(
                        BookingRequest request);

        List<BookingResponse> getMyBookings();

        BookingResponse getBookingById(Long id);

        BookingResponse cancelBooking(Long bookingId, String userEmail);

        BookingStatusResponse isEventBooked(
                        Long eventId,
                        String userEmail);
}