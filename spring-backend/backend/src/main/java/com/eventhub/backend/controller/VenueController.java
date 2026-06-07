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
        
        // Ensure searchTerm is never null
        if (searchTerm == null) {
            searchTerm = "";
        }
        
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = endDateStr != null ? LocalDate.parse(endDateStr) : startDate;

        List<Venue> allVenues = venueRepository.findAll();
        
        Set<Long> occupiedVenueIds = new HashSet<>();
        List<Event> overlappingEvents = eventRepository.findAll();
        for (Event event : overlappingEvents) {
            LocalDate eventStart = event.getStartDate();
            LocalDate eventEnd = event.getEndDate() != null ? event.getEndDate() : eventStart;
            
            if (!eventStart.isAfter(endDate) && !eventEnd.isBefore(startDate)) {
                if (event.getVenue() != null) {
                    occupiedVenueIds.add(event.getVenue().getId());
                }
            }
        }
        
        List<VenueResponse> availableVenues = new ArrayList<>();
        Set<Long> addedVenueIds = new HashSet<>();
        String lowerSearchTerm = searchTerm.toLowerCase().trim();
        
        for (Venue venue : allVenues) {
            // Skip if already added or occupied
            if (addedVenueIds.contains(venue.getId()) || occupiedVenueIds.contains(venue.getId())) {
                continue;
            }
            
            // Apply search filter if search term is not empty and not "*"
            boolean matchesSearch = true;
            if (!lowerSearchTerm.isEmpty() && !"*".equals(lowerSearchTerm)) {
                String venueLabel = (venue.getName() + (venue.getCity() != null ? " " + venue.getCity() : "")).toLowerCase();
                matchesSearch = venueLabel.contains(lowerSearchTerm);
            }
            
            if (matchesSearch) {
                availableVenues.add(venueService.mapToResponse(venue));
                addedVenueIds.add(venue.getId());
            }
        }
        
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
                
        return new ApiResponse<>(true, message, availableVenues);
    }

    @GetMapping("/{id}")
    public ApiResponse<VenueResponse> getVenueByIdLegacy(
            @PathVariable Long id) {
        VenueResponse response = venueService.getVenueById(id);
        return new ApiResponse<>(true, "Venue fetched successfully", response);
    }
}