package com.eventhub.backend.dto;

import com.eventhub.backend.entity.Session;

public class SessionTicketStatsDTO {
    private Session session;
    private String type;
    private Long sold;
    private Double revenue;

    public SessionTicketStatsDTO() {}

    public SessionTicketStatsDTO(Session session, String type, Long sold, Double revenue) {
        this.session = session;
        this.type = type;
        this.sold = sold;
        this.revenue = revenue;
    }

    public SessionTicketStatsDTO(Session session, String type, Number sold, Number revenue) {
        this.session = session;
        this.type = type;
        this.sold = sold != null ? sold.longValue() : 0L;
        this.revenue = revenue != null ? revenue.doubleValue() : 0.0;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getSold() {
        return sold;
    }

    public void setSold(Long sold) {
        this.sold = sold;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }
}
