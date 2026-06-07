package com.eventhub.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRequest {
    private String type;
    private String section;
    private Double price;
    private Integer capacity;
    private Integer available;
    private Integer totalSeats;
}
