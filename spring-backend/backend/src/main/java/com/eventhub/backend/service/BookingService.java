package com.eventhub.backend.service;

import com.eventhub.backend.dto.BookingRequest;
import com.eventhub.backend.dto.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(
            BookingRequest request);

    List<BookingResponse> getMyBookings();

    BookingResponse getBookingById(Long id);
}