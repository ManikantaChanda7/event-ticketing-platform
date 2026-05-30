package com.eventhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long venueId;

    @NotBlank
    private String category;

    private String bannerImage;

    private String status;
}