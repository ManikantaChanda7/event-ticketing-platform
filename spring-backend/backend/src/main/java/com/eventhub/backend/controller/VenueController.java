package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.VenueRequest;
import com.eventhub.backend.dto.VenueResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.VenueRepository;
import com.eventhub.backend.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public VenueController(VenueService venueService, VenueRepository venueRepository, EventRepository eventRepository) {
        this.venueService = venueService;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
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

@RestController
@RequestMapping("/api/venue")
class VenueLegacyController {

    private final VenueService venueService;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    public VenueLegacyController(VenueService venueService, VenueRepository venueRepository, EventRepository eventRepository) {
        this.venueService = venueService;
        this.venueRepository = venueRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping("/availability")
    public ApiResponse<List<VenueResponse>> getAvailableVenues(
            @RequestParam(name = "startDate", required = true) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr,
            @RequestParam(name = "search", required = false, defaultValue = "") String searchTerm) {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : startDate;

        // Use optimized single query instead of fetching all venues and events
        List<Venue> availableVenues = venueRepository.findAvailableVenues(startDate, endDate, searchTerm);

        List<VenueResponse> venueResponses = availableVenues.stream()
                .map(venueService::mapToResponse)
                .collect(Collectors.toList());

        String message;
        if (startDate.equals(endDate)) {
            message = searchTerm != null && !searchTerm.isEmpty()
                    ? "Available venues for " + startDate + " matching '" + searchTerm + "'"
                    : "Available venues for " + startDate;
        } else {
            message = searchTerm != null && !searchTerm.isEmpty()
                    ? "Available venues for " + startDate + " to " + endDate + " matching '" + searchTerm + "'"
                    : "Available venues for " + startDate + " to " + endDate;
        }

        return new ApiResponse<>(true, message, venueResponses);
    }

    @GetMapping("/{id}")
    public ApiResponse<VenueResponse> getVenueByIdLegacy(
            @PathVariable Long id) {
        VenueResponse response = venueService.getVenueById(id);
        return new ApiResponse<>(true, "Venue fetched successfully", response);
    }
}