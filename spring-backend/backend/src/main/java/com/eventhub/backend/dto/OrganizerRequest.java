package com.eventhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizerRequest {

    @NotBlank
    private String organizationName; // Keep old name for backward compatibility with request

    private String orgEmail;

    private String description; // Keep old name for backward compatibility with request

    private String organizerProfileImage;

    private String organizerBannerImage;
}