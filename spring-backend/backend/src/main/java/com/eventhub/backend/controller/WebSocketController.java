package com.eventhub.backend.controller;

import com.eventhub.backend.dto.SeatStatusUpdate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/seat/update")
    @SendTo("/topic/seats")
    public SeatStatusUpdate broadcastSeatUpdate(SeatStatusUpdate update) {
        update.setTimestamp(System.currentTimeMillis());
        return update;
    }
}
