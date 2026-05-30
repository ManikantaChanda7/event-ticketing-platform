package com.eventhub.backend.dto;

import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @NotNull
    private Long bookingId;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }
}