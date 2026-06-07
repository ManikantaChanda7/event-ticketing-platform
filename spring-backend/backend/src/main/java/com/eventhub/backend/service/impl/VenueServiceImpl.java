package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.VenueRequest;
import com.eventhub.backend.dto.VenueResponse;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.VenueRepository;
import com.eventhub.backend.service.VenueService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final ObjectMapper objectMapper;

    public VenueServiceImpl(VenueRepository venueRepository, ObjectMapper objectMapper) {
        this.venueRepository = venueRepository;
        this.objectMapper = objectMapper;
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

    public VenueResponse mapToResponse(Venue venue) {
        List<Map<String, Object>> seatingLayout = null;
        if (venue.getSeatingLayout() != null && !venue.getSeatingLayout().isEmpty()) {
            try {
                // Parse the seating layout from object format to list format
                Map<String, Object> layoutMap = objectMapper.readValue(venue.getSeatingLayout(),
                        new TypeReference<Map<String, Object>>() {
                        });
                seatingLayout = new ArrayList<>();

                for (Map.Entry<String, Object> entry : layoutMap.entrySet()) {
                    Map<String, Object> sectionData = new HashMap<>();
                    sectionData.put("section", entry.getKey());
                    @SuppressWarnings("unchecked")
                    Map<String, Object> sectionDetails = (Map<String, Object>) entry.getValue();
                    sectionData.putAll(sectionDetails);
                    seatingLayout.add(sectionData);
                }
            } catch (Exception e) {
                seatingLayout = null;
            }
        }

        Map<String, Object> location = null;
        if (venue.getLatitude() != null && venue.getLongitude() != null) {
            location = new HashMap<>();
            location.put("type", "Point");
            location.put("coordinates", List.of(venue.getLongitude(), venue.getLatitude()));
        }

        return VenueResponse.builder()
                .id(venue.getId())
                ._id(venue.getId())
                .name(venue.getName())
                .address(venue.getAddress())
                .city(venue.getCity())
                .state(venue.getState())
                .country(venue.getCountry())
                .capacity(venue.getCapacity())
                .latitude(venue.getLatitude())
                .longitude(venue.getLongitude())
                .seatingLayout(seatingLayout)
                .location(location)
                .build();
    }
}