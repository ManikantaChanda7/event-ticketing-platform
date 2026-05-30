package com.eventhub.backend.dto;

public class BookingStatusResponse {

    private boolean booked;

    public BookingStatusResponse() {
    }

    public BookingStatusResponse(boolean booked) {
        this.booked = booked;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}