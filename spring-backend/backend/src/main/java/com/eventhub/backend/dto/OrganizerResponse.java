package com.eventhub.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrganizerResponse {

    private Long _id;
    private Long id;
    private Long user;
    private String phone;
    private String organizerProfileImage;
    private String orgName;
    private String orgEmail;
    private String orgDescription;
    private String organizerBannerImage;
    private List<String> orgSpecialities;
    private Double averageRating;
    private Integer totalReviews;
    private List<Long> eventsHosted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}