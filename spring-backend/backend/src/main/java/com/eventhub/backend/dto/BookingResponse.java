package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class BookingResponse {

    private Long _id;
    private Long id;
    private Long user;
    private Long event;
    private Long session;
    private List<BookedSeat> seats;
    private List<TicketSummary> ticketsSummary;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    public static class BookedSeat {
        private String seatId;
        private String section;
        private BigDecimal price;
    }

    @Getter
    @Setter
    public static class TicketSummary {
        private String type;
        private Integer quantity;
        private BigDecimal totalPrice;
    }
}