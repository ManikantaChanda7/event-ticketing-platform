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
    private EventDTO event;
    private SessionDTO session;
    private List<BookedSeat> seats;
    private List<TicketSummary> ticketsSummary;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @Builder
    public static class EventDTO {
        private Long id;
        private String title;
        private String description;
        private String category;
        private String locationType;
        private LocationDTO location;
        private String bannerImage;
        private String thumbnailImage;
        private Double averageRating;
        private Integer interestedUsers;
    }

    @Getter
    @Setter
    @Builder
    public static class LocationDTO {
        private String label;
        private String address;
        private String city;
        private Double latitude;
        private Double longitude;
    }

    @Getter
    @Setter
    @Builder
    public static class SessionDTO {
        private Long id;
        private String date;
        private String startTime;
        private String endTime;
    }

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