package com.eventhub.backend.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {

    @NotNull
    private Long eventId;

    @NotNull
    private Long sessionId;

    @NotEmpty
    private List<SelectedSeat> selectedSeats;

    @Getter
    @Setter
    public static class SelectedSeat {
        @NotNull
        private String seatId;

        @NotNull
        private String section;

        @NotNull
        @Min(0)
        private Double price;
    }
}