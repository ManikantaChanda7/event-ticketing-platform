package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
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
    public ApiResponse<TicketTypeResponse> createTicketType(
            @Valid @RequestBody TicketTypeRequest request) {
        TicketTypeResponse response = ticketTypeService.createTicketType(request);
        return new ApiResponse<>(true, "Ticket type created successfully", response);
    }

    @GetMapping
    public ApiResponse<List<TicketTypeResponse>> getAllTicketTypes() {
        List<TicketTypeResponse> response = ticketTypeService.getAllTicketTypes();
        return new ApiResponse<>(true, "All ticket types fetched successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<TicketTypeResponse> getTicketTypeById(
            @PathVariable Long id) {
        TicketTypeResponse response = ticketTypeService.getTicketTypeById(id);
        return new ApiResponse<>(true, "Ticket type fetched successfully", response);
    }

    @GetMapping("/session/{sessionId}")
    public ApiResponse<List<TicketTypeResponse>> getTicketTypesBySession(
            @PathVariable Long sessionId) {
        List<TicketTypeResponse> response = ticketTypeService.getTicketTypesBySession(sessionId);
        return new ApiResponse<>(true, "Ticket types by session fetched successfully", response);
    }
}