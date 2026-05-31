package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
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
    public ApiResponse<VenueResponse> createVenue(
            @Valid @RequestBody VenueRequest request) {
        VenueResponse response = venueService.createVenue(request);
        return new ApiResponse<>(true, "Venue created successfully", response);
    }

    @GetMapping
    public ApiResponse<List<VenueResponse>> getAllVenues() {
        List<VenueResponse> response = venueService.getAllVenues();
        return new ApiResponse<>(true, "All venues fetched successfully", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<VenueResponse> getVenueById(
            @PathVariable Long id) {
        VenueResponse response = venueService.getVenueById(id);
        return new ApiResponse<>(true, "Venue fetched successfully", response);
    }

    @PutMapping("/{id}")
    public ApiResponse<VenueResponse> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest request) {
        VenueResponse response = venueService.updateVenue(id, request);
        return new ApiResponse<>(true, "Venue updated successfully", response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVenue(
            @PathVariable Long id) {
        venueService.deleteVenue(id);
        return new ApiResponse<>(true, "Venue deleted successfully", null);
    }
}