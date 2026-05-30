package com.eventhub.backend.dto;

import java.util.List;

public class LocationResponse {

    private String label;

    private List<Double> coordinates;

    public LocationResponse() {
    }

    public LocationResponse(
            String label,
            List<Double> coordinates) {

        this.label = label;
        this.coordinates = coordinates;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Double> coordinates) {
        this.coordinates = coordinates;
    }
}