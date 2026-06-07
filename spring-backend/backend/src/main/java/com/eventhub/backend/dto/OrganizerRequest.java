package com.eventhub.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizerRequest {

    @NotBlank
    private String orgName;

    @NotBlank
    @Email
    private String orgEmail;

    private String phone;

    private String orgDescription;

    private String organizerProfileImage;

    private String organizerBannerImage;

    // Keep old fields for backward compatibility
    private String organizationName;
    private String description;
}