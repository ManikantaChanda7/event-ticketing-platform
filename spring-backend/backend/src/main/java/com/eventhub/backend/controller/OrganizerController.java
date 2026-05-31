package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.OrganizerRequest;
import com.eventhub.backend.dto.OrganizerResponse;
import com.eventhub.backend.service.OrganizerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizers")
public class OrganizerController {

    private final OrganizerService organizerService;

    public OrganizerController(
            OrganizerService organizerService) {

        this.organizerService = organizerService;
    }

    @GetMapping("/ping")
    public ApiResponse<String> ping() {
        return new ApiResponse<>(true, "pong", "pong");
    }

    @PostMapping
    public ApiResponse<OrganizerResponse> createOrganizer(
            @Valid @RequestBody OrganizerRequest request) {
        System.out.println("CREATE ORGANIZER HIT");
        OrganizerResponse response = organizerService.createOrganizer(request);
        return new ApiResponse<>(true, "Organizer created successfully", response);
    }

    @GetMapping
    public ApiResponse<List<OrganizerResponse>> getAllOrganizers() {
        List<OrganizerResponse> response = organizerService.getAllOrganizers();
        return new ApiResponse<>(true, "All organizers fetched successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrganizerResponse> getOrganizerById(
            @PathVariable Long id) {
        OrganizerResponse response = organizerService.getOrganizerById(id);
        return new ApiResponse<>(true, "Organizer fetched successfully", response);
    }
}