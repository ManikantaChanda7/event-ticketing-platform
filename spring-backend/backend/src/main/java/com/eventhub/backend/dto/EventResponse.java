package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventResponse {

    private Long id;

    private String title;

    private String description;

    private String category;

    private String bannerImage;

    private String status;

    private String organizerName;

    private String venueName;
}