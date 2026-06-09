package com.eventhub.backend.dto;

public class TopSellingEventDTO {
    private Long eventId;
    private String title;
    private Long ticketsSold;
    private Double revenue;

    public TopSellingEventDTO() {}

    public TopSellingEventDTO(Long eventId, String title, Long ticketsSold, Double revenue) {
        this.eventId = eventId;
        this.title = title;
        this.ticketsSold = ticketsSold;
        this.revenue = revenue;
    }

    public TopSellingEventDTO(Number eventId, String title, Number ticketsSold, Number revenue) {
        this.eventId = eventId != null ? eventId.longValue() : null;
        this.title = title;
        this.ticketsSold = ticketsSold != null ? ticketsSold.longValue() : 0L;
        this.revenue = revenue != null ? revenue.doubleValue() : 0.0;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(Long ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }
}
