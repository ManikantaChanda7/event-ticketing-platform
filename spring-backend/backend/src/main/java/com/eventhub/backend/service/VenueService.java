package com.eventhub.backend.service;

import com.eventhub.backend.dto.VenueResponse;
import com.eventhub.backend.entity.Venue;

import java.util.List;

public interface VenueService {

    VenueResponse createVenue(com.eventhub.backend.dto.VenueRequest request);

    List<VenueResponse> getAllVenues();

    VenueResponse getVenueById(Long id);

    VenueResponse updateVenue(Long id, com.eventhub.backend.dto.VenueRequest request);

    void deleteVenue(Long id);

    VenueResponse mapToResponse(Venue venue);
}