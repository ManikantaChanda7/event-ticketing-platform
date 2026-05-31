package com.eventhub.backend.dto;

import java.util.List;

public class LocationResponse {

    private String type; // "Point" for GeoJSON

    private String label;

    private List<Double> coordinates; // [longitude, latitude]

    public LocationResponse() {
    }

    public LocationResponse(
            String type,
            String label,
            List<Double> coordinates) {

        this.type = type;
        this.label = label;
        this.coordinates = coordinates;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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