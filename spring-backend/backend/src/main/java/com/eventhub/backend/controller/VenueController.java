package com.eventhub.backend.controller;

import com.eventhub.backend.dto.VenueRequest;
import com.eventhub.backend.dto.VenueResponse;
import com.eventhub.backend.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public VenueResponse createVenue(
            @Valid @RequestBody VenueRequest request) {

        return venueService.createVenue(request);
    }

    @GetMapping
    public List<VenueResponse> getAllVenues() {
        return venueService.getAllVenues();
    }

    @GetMapping("/{id}")
    public VenueResponse getVenueById(
            @PathVariable Long id) {

        return venueService.getVenueById(id);
    }

    @PutMapping("/{id}")
    public VenueResponse updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest request) {

        return venueService.updateVenue(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteVenue(
            @PathVariable Long id) {

        venueService.deleteVenue(id);
    }
}