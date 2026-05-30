package com.eventhub.backend.service;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventSummaryResponse;

import java.util.List;

public interface EventService {

        EventResponse createEvent(EventRequest request);

        // List<EventResponse> getAllEvents();

        EventResponse getEventById(Long id);

        List<EventResponse> getMyEvents();

        EventResponse updateEvent(Long id, EventRequest request);

        void deleteEvent(Long id);

        List<EventSummaryResponse> getAllEvents();

        List<EventSummaryResponse> getEventsByCategory(
                        String category);

        List<EventSummaryResponse> searchEvents(
                        String keyword);

        List<String> getCategories();

        List<EventSummaryResponse> getSimilarEvents(
                        Long eventId);

        List<EventSummaryResponse> getRecommendedEvents();

        void markInterest(
                        Long eventId,
                        String userEmail);

        void removeInterest(
                        Long eventId,
                        String userEmail);

        List<EventSummaryResponse> getTrendingEvents();

        List<EventSummaryResponse> getPopularEvents();

        List<EventSummaryResponse> getMyInterestedEvents();

        boolean isInterested(
                        Long eventId,
                        String userEmail);

        long getSessionsCount(Long eventId);

        List<EventSummaryResponse> getEventsByOrganizer(Long organizerId);

        List<EventSummaryResponse> filterEvents(
                        String category,
                        String city,
                        String keyword);

}