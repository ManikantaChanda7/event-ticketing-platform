package com.eventhub.backend.dto;

import java.util.List;

public class NodeEventResponse {

    private Long id;

    private String title;

    private String description;

    private String bannerImage;

    private String thumbnailImage;

    private String category;

    private String status;

    private Long organizer;

    private Double averageRating;

    private Integer interestedUsers;

    private Double startingPrice;

    private String recurrence;

    private String startDate;

    private String endDate;

    private String startTime;

    private String endTime;

    private List<String> languages;

    private List<String> selectedWeekdays;

    private LocationResponse location;

    private List<NodeReviewResponse> ratings;

    // Generate getters and setters
}