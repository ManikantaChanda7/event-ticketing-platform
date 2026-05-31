package com.eventhub.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SessionResponse {

    private Long _id;
    private Long id;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private LocalDateTime releaseDate;
    private Long event;
    private List<Ticket> tickets;
    private List<SeatInfo> seats;
    private Integer occupancy;

    @Getter
    @Setter
    public static class Ticket {
        private String type;
        private Double price;
        private Integer available;
        private Integer totalSeats;
    }

    @Getter
    @Setter
    public static class SeatInfo {
        private String seatId;
        private String section;
        private String status;
        private Long user;
    }
}