package com.eventhub.backend.dto;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long _id;

    private Long id;

    private String title;

    private String description;

    private String category;

    private String bannerImage;

    private String status;

    private Double averageRating;

    private Integer interestedUsers;

    private OrganizerSummaryResponse organizer;

    private LocationResponse location;

    private List<NodeReviewResponse> ratings;

    private Double startingPrice;

    private String thumbnailImage;
}