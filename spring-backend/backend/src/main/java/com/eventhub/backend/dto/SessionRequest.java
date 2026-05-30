package com.eventhub.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionRequest {

    @NotNull
    private Long eventId;

    @NotNull
    private String sessionDate;

    @NotNull
    private String startTime;

    @NotNull
    private String endTime;

    @NotNull
    private Integer capacity;
}