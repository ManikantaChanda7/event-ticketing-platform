package com.eventhub.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class OrganizerProfileResponse {
    private Long id;
    private String orgName;
    private String orgEmail;
    private String orgDescription;
    private String phone;
    private String organizerProfileImage;
    private String organizerBannerImage;
    private Set<String> orgSpecialities;
    private Double averageRating;
    private Integer totalReviews;
}
