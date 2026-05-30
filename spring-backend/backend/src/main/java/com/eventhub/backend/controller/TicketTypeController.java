package com.eventhub.backend.controller;

import com.eventhub.backend.dto.TicketTypeRequest;
import com.eventhub.backend.dto.TicketTypeResponse;
import com.eventhub.backend.service.TicketTypeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-types")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(
            TicketTypeService ticketTypeService) {

        this.ticketTypeService = ticketTypeService;
    }

    @PostMapping
    public TicketTypeResponse createTicketType(
            @Valid @RequestBody TicketTypeRequest request) {

        return ticketTypeService.createTicketType(request);
    }

    @GetMapping
    public List<TicketTypeResponse> getAllTicketTypes() {

        return ticketTypeService.getAllTicketTypes();
    }

    @GetMapping("/{id}")
    public TicketTypeResponse getTicketTypeById(
            @PathVariable Long id) {

        return ticketTypeService.getTicketTypeById(id);
    }

    @GetMapping("/session/{sessionId}")
    public List<TicketTypeResponse> getTicketTypesBySession(
            @PathVariable Long sessionId) {

        return ticketTypeService.getTicketTypesBySession(
                sessionId);
    }
}