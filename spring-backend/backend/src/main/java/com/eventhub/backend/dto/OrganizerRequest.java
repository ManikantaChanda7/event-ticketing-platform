package com.eventhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizerRequest {

    @NotBlank
    private String organizationName;

    private String website;

    private String description;
}