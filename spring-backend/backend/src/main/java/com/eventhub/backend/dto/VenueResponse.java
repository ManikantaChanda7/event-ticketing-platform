package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VenueResponse {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String country;
    private Integer capacity;
    private Double latitude;
    private Double longitude;
}