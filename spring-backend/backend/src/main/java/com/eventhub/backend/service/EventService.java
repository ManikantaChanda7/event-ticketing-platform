package com.eventhub.backend.service;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.dto.PaginatedResponse;

import java.time.LocalDate;
import java.util.List;

public interface EventService {

        EventResponse createEvent(EventRequest request);

        // List<EventResponse> getAllEvents();

        EventResponse getEventById(Long id);

        List<EventResponse> getMyEvents();

        List<EventSummaryResponse> getMyEventsSummary();

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

        Integer markInterest(
                        Long eventId,
                        String userEmail);

        Integer removeInterest(
                        Long eventId,
                        String userEmail);

        List<EventSummaryResponse> getTrendingEvents();

        List<EventSummaryResponse> getPopularEvents();

        PaginatedResponse<EventSummaryResponse> getMyInterestedEvents(Integer page, Integer limit, String status);

        boolean isInterested(
                        Long eventId,
                        String userEmail);

        long getSessionsCount(Long eventId);

        PaginatedResponse<EventSummaryResponse> getEventsByOrganizer(Long organizerId, Integer page, Integer limit);

        PaginatedResponse<EventSummaryResponse> filterEvents(
                        List<String> categories,
                        String city,
                        String keyword,
                        Boolean isFeatured,
                        Boolean location,
                        Integer page,
                        Integer limit,
                        String recurrence,
                        Double minRating,
                        LocalDate startDate,
                        LocalDate endDate,
                        List<String> languages,
                        Integer ageLimit,
                        String startTime,
                        String endTime);

}