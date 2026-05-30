package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionResponse {

    private Long id;

    private String sessionDate;

    private String startTime;

    private String endTime;

    private Integer capacity;

    private Integer availableSeats;

    private String eventTitle;
}