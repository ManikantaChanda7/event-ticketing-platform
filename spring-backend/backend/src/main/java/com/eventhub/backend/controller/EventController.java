package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.dto.PaginatedResponse;
import com.eventhub.backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

        private final EventService eventService;

        public EventController(EventService eventService) {
                this.eventService = eventService;
        }

        @PostMapping
        public ApiResponse<EventResponse> createEvent(
                        @Valid @RequestBody EventRequest request) {
                EventResponse response = eventService.createEvent(request);
                return new ApiResponse<>(true, "Event created successfully", response);
        }

        // @GetMapping
        // public List<EventResponse> getAllEvents() {
        // return eventService.getAllEvents();
        // }

        @GetMapping("/{id}")
        public ApiResponse<EventResponse> getEventById(
                        @PathVariable Long id) {
                EventResponse response = eventService.getEventById(id);
                return new ApiResponse<>(true, "Event fetched successfully", response);
        }

        @GetMapping("/my-events")
        public ApiResponse<List<EventSummaryResponse>> getMyEvents() {
                List<EventSummaryResponse> response = eventService.getMyEventsSummary();
                return new ApiResponse<>(true, "My events fetched successfully", response);
        }

        @PutMapping("/{id}")
        public ApiResponse<EventResponse> updateEvent(
                        @PathVariable Long id,
                        @Valid @RequestBody EventRequest request) {
                EventResponse response = eventService.updateEvent(id, request);
                return new ApiResponse<>(true, "Event updated successfully", response);
        }

        @PutMapping("/{id}/updateEventImage")
        public ApiResponse<EventResponse> updateEventImage(
                        @PathVariable Long id,
                        @RequestBody Map<String, String> request) {
                EventResponse response = eventService.updateEventImage(id, request);
                return new ApiResponse<>(true, "Event image updated successfully", response);
        }

        @DeleteMapping("/{id}")
        public ApiResponse<Void> deleteEvent(
                        @PathVariable Long id) {
                eventService.deleteEvent(id);
                return new ApiResponse<>(true, "Event deleted successfully", null);
        }

        @GetMapping("/all")
        public ApiResponse<List<EventSummaryResponse>> getAllEvents() {
                List<EventSummaryResponse> response = eventService.getAllEvents();
                return new ApiResponse<>(true, "All events fetched successfully", response);
        }

        @GetMapping("/category/{category}")
        public ApiResponse<List<EventSummaryResponse>> getEventsByCategory(
                        @PathVariable String category) {
                List<EventSummaryResponse> response = eventService.getEventsByCategory(category);
                return new ApiResponse<>(true, "Events by category fetched successfully", response);
        }

        @GetMapping("/search")
        public ApiResponse<List<EventSummaryResponse>> searchEvents(
                        @RequestParam String keyword) {
                List<EventSummaryResponse> response = eventService.searchEvents(keyword);
                return new ApiResponse<>(true, "Events searched successfully", response);
        }

        @GetMapping("/categories")
        public ApiResponse<List<String>> getCategories() {
                List<String> response = eventService.getCategories();
                return new ApiResponse<>(true, "Categories fetched successfully", response);
        }

        @GetMapping("/{eventId}/similarEvents")
        public ApiResponse<List<EventSummaryResponse>> getSimilarEvents(
                        @PathVariable Long eventId) {
                List<EventSummaryResponse> response = eventService.getSimilarEvents(eventId);
                return new ApiResponse<>(true, "Similar events fetched successfully", response);
        }

        @GetMapping("/recommendedEvents")
        public ApiResponse<List<EventSummaryResponse>> getRecommendedEvents() {
                List<EventSummaryResponse> response = eventService.getRecommendedEvents();
                return new ApiResponse<>(true, "Recommended events fetched successfully", response);
        }

        @PostMapping("/{eventId}/interest")
        public ApiResponse<Integer> markInterest(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                Integer interestedUsers = eventService.markInterest(eventId, authentication.getName());
                return new ApiResponse<>(true, "Interest added successfully", interestedUsers);
        }

        @DeleteMapping("/{eventId}/interest")
        public ApiResponse<Integer> removeInterest(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                Integer interestedUsers = eventService.removeInterest(eventId, authentication.getName());
                return new ApiResponse<>(true, "Interest removed successfully", interestedUsers);
        }

        @GetMapping("/trendingEvents")
        public ApiResponse<List<EventSummaryResponse>> getTrendingEvents() {
                List<EventSummaryResponse> response = eventService.getTrendingEvents();
                return new ApiResponse<>(true, "Trending events fetched successfully", response);
        }

        @GetMapping("/popularEvents")
        public ApiResponse<List<EventSummaryResponse>> getPopularEvents() {
                List<EventSummaryResponse> response = eventService.getPopularEvents();
                return new ApiResponse<>(true, "Popular events fetched successfully", response);
        }

        @GetMapping("/user/interests")
        public ApiResponse<PaginatedResponse<EventSummaryResponse>> getMyInterestedEvents(
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer limit,
                        @RequestParam(required = false) String status) {
                PaginatedResponse<EventSummaryResponse> response = eventService.getMyInterestedEvents(page, limit,
                                status);
                return new ApiResponse<>(true, "My interested events fetched successfully", response);
        }

        @GetMapping("/{eventId}/interest-status")
        public ApiResponse<Boolean> isInterested(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                Boolean response = eventService.isInterested(eventId, authentication.getName());
                return new ApiResponse<>(true, "Interest status fetched successfully", response);
        }

        @GetMapping("/{eventId}/sessionsCount")
        public ApiResponse<Map<String, Object>> getSessionsCount(
                        @PathVariable Long eventId) {
                long count = eventService.getSessionsCount(eventId);
                Map<String, Object> response = new HashMap<>();
                response.put("count", count);
                response.put("sessionsAvailable", count > 0);
                return new ApiResponse<>(true, "Sessions count fetched successfully", response);
        }

        @GetMapping("/organizer/{organizerId}")
        public ApiResponse<PaginatedResponse<EventSummaryResponse>> getEventsByOrganizer(
                        @PathVariable Long organizerId,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer limit) {
                PaginatedResponse<EventSummaryResponse> response = eventService.getEventsByOrganizer(organizerId, page,
                                limit);
                return new ApiResponse<>(true, "Events by organizer fetched successfully", response);
        }

        @GetMapping("/filtered")
        public ApiResponse<PaginatedResponse<EventSummaryResponse>> filterEvents(
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) Boolean isFeatured,
                        @RequestParam(required = false) Boolean location,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer limit,
                        @RequestParam(required = false) String recurrence,
                        @RequestParam(required = false) Double minRating,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false) String language,
                        @RequestParam(required = false) Integer age,
                        @RequestParam(required = false) Integer ageLimit,
                        @RequestParam(required = false) String startTime,
                        @RequestParam(required = false) String endTime) {
                // Use search if keyword is not provided
                String effectiveKeyword = keyword != null ? keyword : search;
                // Use age if ageLimit is not provided
                Integer effectiveAgeLimit = ageLimit != null ? ageLimit : age;

                List<String> categories = parseCsv(category);
                List<String> languages = parseCsv(language);
                LocalDate startDateObj = parseDate(startDate);
                LocalDate endDateObj = parseDate(endDate);

                PaginatedResponse<EventSummaryResponse> response = eventService.filterEvents(
                                categories,
                                city,
                                effectiveKeyword,
                                isFeatured,
                                location,
                                page,
                                limit,
                                recurrence,
                                minRating,
                                startDateObj,
                                endDateObj,
                                languages,
                                effectiveAgeLimit,
                                startTime,
                                endTime);
                return new ApiResponse<>(true, "Filtered events fetched successfully", response);
        }

        private List<String> parseCsv(String input) {
                if (input == null || input.isBlank()) {
                        return null;
                }
                List<String> values = Arrays.stream(input.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .collect(Collectors.toList());
                return values.isEmpty() ? null : values;
        }

        private LocalDate parseDate(String date) {
                if (date == null || date.isBlank()) {
                        return null;
                }
                try {
                        // Handle ISO 8601 format with time (e.g., "2026-06-23T18:30:00.000Z")
                        if (date.contains("T")) {
                                return java.time.Instant.parse(date).atZone(java.time.ZoneId.systemDefault())
                                                .toLocalDate();
                        }
                        // Handle simple date format (e.g., "2026-06-23")
                        return LocalDate.parse(date);
                } catch (Exception e) {
                        return null;
                }
        }
}
