package com.eventhub.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusUpdate {
    private Long sessionId;
    private String seatId;
    private String status;
    private Long userId;
    private Long timestamp;
}
