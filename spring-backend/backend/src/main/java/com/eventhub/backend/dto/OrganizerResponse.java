package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizerResponse {

    private Long id;

    private String organizationName;

    private String website;

    private String description;

    private Boolean verified;
}