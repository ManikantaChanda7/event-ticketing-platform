package com.eventhub.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class BookingResponse {

    private Long id;

    private String username;

    private String eventTitle;

    private String ticketName;

    private Integer quantity;

    private BigDecimal totalAmount;

    private String status;
}