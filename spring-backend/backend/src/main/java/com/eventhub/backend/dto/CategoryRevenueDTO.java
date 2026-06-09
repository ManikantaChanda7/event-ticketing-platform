package com.eventhub.backend.dto;

public class CategoryRevenueDTO {
    private String category;
    private Double revenue;

    public CategoryRevenueDTO() {}

    public CategoryRevenueDTO(String category, Double revenue) {
        this.category = category;
        this.revenue = revenue;
    }

    public CategoryRevenueDTO(String category, Number revenue) {
        this.category = category;
        this.revenue = revenue != null ? revenue.doubleValue() : 0.0;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }
}
