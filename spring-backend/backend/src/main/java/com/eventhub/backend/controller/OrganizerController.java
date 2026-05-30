package com.eventhub.backend.controller;

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
    public String ping() {
        return "pong";
    }

    @PostMapping
    public OrganizerResponse createOrganizer(
            @Valid @RequestBody OrganizerRequest request) {

        System.out.println("CREATE ORGANIZER HIT");

        return organizerService.createOrganizer(request);
    }

    @GetMapping
    public List<OrganizerResponse> getAllOrganizers() {

        return organizerService.getAllOrganizers();
    }

    @GetMapping("/{id}")
    public OrganizerResponse getOrganizerById(
            @PathVariable Long id) {

        return organizerService.getOrganizerById(id);
    }
}