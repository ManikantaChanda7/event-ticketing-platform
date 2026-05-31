package com.eventhub.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private String thumbnailImage;

    private String status;

    private Double averageRating;

    private Integer interestedUsers;

    private OrganizerSummaryResponse organizer;

    private LocationResponse location;

    private List<NodeReviewResponse> ratings;

    private Double startingPrice;

    // Node.js matching fields
    private LocalDate startDate;

    private LocalDate endDate;

    private String startTime; // "HH:mm" format

    private String endTime; // "HH:mm" format

    private String recurrence; // "single", "multi-day", "weekly"

    private List<String> selectedWeekdays;

    private Integer ageLimit;

    private List<String> languages;

    private Boolean isFeatured;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Venue as ID to match Node.js
    private Long venue;
}