package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        public ApiResponse<List<EventResponse>> getMyEvents() {
                List<EventResponse> response = eventService.getMyEvents();
                return new ApiResponse<>(true, "My events fetched successfully", response);
        }

        @PutMapping("/{id}")
        public ApiResponse<EventResponse> updateEvent(
                        @PathVariable Long id,
                        @Valid @RequestBody EventRequest request) {
                EventResponse response = eventService.updateEvent(id, request);
                return new ApiResponse<>(true, "Event updated successfully", response);
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
        public ApiResponse<Void> markInterest(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                eventService.markInterest(eventId, authentication.getName());
                return new ApiResponse<>(true, "Interest added successfully", null);
        }

        @DeleteMapping("/{eventId}/interest")
        public ApiResponse<Void> removeInterest(
                        @PathVariable Long eventId,
                        Authentication authentication) {
                eventService.removeInterest(eventId, authentication.getName());
                return new ApiResponse<>(true, "Interest removed successfully", null);
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
        public ApiResponse<List<EventSummaryResponse>> getMyInterestedEvents() {
                List<EventSummaryResponse> response = eventService.getMyInterestedEvents();
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
        public ApiResponse<Long> getSessionsCount(
                        @PathVariable Long eventId) {
                Long response = eventService.getSessionsCount(eventId);
                return new ApiResponse<>(true, "Sessions count fetched successfully", response);
        }

        @GetMapping("/organizer/{organizerId}")
        public ApiResponse<List<EventSummaryResponse>> getEventsByOrganizer(
                        @PathVariable Long organizerId) {
                List<EventSummaryResponse> response = eventService.getEventsByOrganizer(organizerId);
                return new ApiResponse<>(true, "Events by organizer fetched successfully", response);
        }

        @GetMapping("/filtered")
        public ApiResponse<List<EventSummaryResponse>> filterEvents(
                        @RequestParam(required = false) String category,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String keyword) {
                List<EventSummaryResponse> response = eventService.filterEvents(category, city, keyword);
                return new ApiResponse<>(true, "Filtered events fetched successfully", response);
        }
}
