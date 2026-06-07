package com.eventhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private Long venue;
    private Long venueId;

    @NotBlank
    private String category;

    private String recurrence;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private Set<String> selectedWeekdays;
    private LocationRequest location;
    private Integer ageLimit;
    private Set<String> languages;
    private String thumbnailImage;
    private String bannerImage;
    private List<TicketRequest> tickets;
    private String status;

    // Getter that returns venueId if present, otherwise venue
    public Long getVenueId() {
        return venueId != null ? venueId : venue;
    }
}
