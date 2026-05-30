package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.VenueRequest;
import com.eventhub.backend.dto.VenueResponse;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.VenueRepository;
import com.eventhub.backend.service.VenueService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    public VenueServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    public VenueResponse createVenue(VenueRequest request) {

        Venue venue = new Venue();

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setState(request.getState());
        venue.setCountry(request.getCountry());
        venue.setCapacity(request.getCapacity());
        venue.setLatitude(request.getLatitude());
        venue.setLongitude(request.getLongitude());

        venue = venueRepository.save(venue);

        return mapToResponse(venue);
    }

    @Override
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public VenueResponse getVenueById(Long id) {

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        return mapToResponse(venue);
    }

    @Override
    public VenueResponse updateVenue(Long id, VenueRequest request) {

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setState(request.getState());
        venue.setCountry(request.getCountry());
        venue.setCapacity(request.getCapacity());
        venue.setLatitude(request.getLatitude());
        venue.setLongitude(request.getLongitude());

        venue = venueRepository.save(venue);

        return mapToResponse(venue);
    }

    @Override
    public void deleteVenue(Long id) {
        venueRepository.deleteById(id);
    }

    private VenueResponse mapToResponse(Venue venue) {

        return VenueResponse.builder()
                .id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .state(venue.getState())
                .country(venue.getCountry())
                .capacity(venue.getCapacity())
                .latitude(venue.getLatitude())
                .longitude(venue.getLongitude())
                .build();
    }
}