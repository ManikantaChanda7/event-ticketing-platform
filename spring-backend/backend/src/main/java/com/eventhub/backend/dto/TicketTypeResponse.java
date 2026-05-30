package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TicketTypeResponse {

    private Long id;

    private String name;

    private BigDecimal price;

    private Integer quantity;

    private Integer remainingQuantity;

    private String sessionDate;

    private String eventTitle;
}