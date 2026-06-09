package com.eventhub.backend.dto;

public class TicketTypeStatsDTO {
    private String type;
    private Long sold;
    private Double revenue;

    public TicketTypeStatsDTO() {}

    public TicketTypeStatsDTO(String type, Long sold, Double revenue) {
        this.type = type;
        this.sold = sold;
        this.revenue = revenue;
    }

    public TicketTypeStatsDTO(String type, Number sold, Number revenue) {
        this.type = type;
        this.sold = sold != null ? sold.longValue() : 0L;
        this.revenue = revenue != null ? revenue.doubleValue() : 0.0;
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
