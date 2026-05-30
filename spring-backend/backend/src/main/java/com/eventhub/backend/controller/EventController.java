package com.eventhub.backend.controller;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.service.EventService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

        private final EventService eventService;

        public EventController(EventService eventService) {
                this.eventService = eventService;
        }

        @PostMapping
        public EventResponse createEvent(
                        @Valid @RequestBody EventRequest request) {

                return eventService.createEvent(request);
        }

        // @GetMapping
        // public List<EventResponse> getAllEvents() {
        // return eventService.getAllEvents();
        // }

        @GetMapping("/{id}")
        public ResponseEntity<?> getEventById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "message", "Event fetched successfully",
                                                "data", eventService.getEventById(id)));
        }

        @GetMapping("/my-events")
        public List<EventResponse> getMyEvents() {

                return eventService.getMyEvents();
        }

        @PutMapping("/{id}")
        public EventResponse updateEvent(
                        @PathVariable Long id,
                        @Valid @RequestBody EventRequest request) {

                return eventService.updateEvent(id, request);
        }

        @DeleteMapping("/{id}")
        public String deleteEvent(
                        @PathVariable Long id) {

                eventService.deleteEvent(id);

                return "Event deleted successfully";
        }

        @GetMapping("/all")
        public ResponseEntity<List<EventSummaryResponse>> getAllEvents() {

                return ResponseEntity.ok(
                                eventService.getAllEvents());
        }

        @GetMapping("/category/{category}")
        public ResponseEntity<List<EventSummaryResponse>> getEventsByCategory(
                        @PathVariable String category) {

                return ResponseEntity.ok(
                                eventService.getEventsByCategory(
                                                category));
        }

        @GetMapping("/search")
        public ResponseEntity<List<EventSummaryResponse>> searchEvents(
                        @RequestParam String keyword) {

                return ResponseEntity.ok(
                                eventService.searchEvents(
                                                keyword));
        }

        @GetMapping("/categories")
        public ResponseEntity<List<String>> getCategories() {
                return ResponseEntity.ok(
                                eventService.getCategories());
        }

        @GetMapping("/{eventId}/similarEvents")
        public ResponseEntity<List<EventSummaryResponse>> getSimilarEvents(
                        @PathVariable Long eventId) {

                return ResponseEntity.ok(
                                eventService.getSimilarEvents(
                                                eventId));
        }

        @GetMapping("/recommendedEvents")
        public ResponseEntity<?> getRecommendedEvents() {

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "data",
                                                eventService.getRecommendedEvents()));
        }

        @PostMapping("/{eventId}/interest")
        public ResponseEntity<String> markInterest(
                        @PathVariable Long eventId,
                        Authentication authentication) {

                eventService.markInterest(
                                eventId,
                                authentication.getName());

                return ResponseEntity.ok(
                                "Interest added");
        }

        @DeleteMapping("/{eventId}/interest")
        public ResponseEntity<String> removeInterest(
                        @PathVariable Long eventId,
                        Authentication authentication) {

                eventService.removeInterest(
                                eventId,
                                authentication.getName());

                return ResponseEntity.ok(
                                "Interest removed");
        }

        @GetMapping("/trendingEvents")
        public ResponseEntity<?> getTrendingEvents() {

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "data", eventService.getTrendingEvents()));
        }

        @GetMapping("/popularEvents")
        public ResponseEntity<?> getPopularEvents() {

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "data", eventService.getPopularEvents()));
        }

        @GetMapping("/user/interests")
        public ResponseEntity<List<EventSummaryResponse>> getMyInterestedEvents() {

                return ResponseEntity.ok(
                                eventService.getMyInterestedEvents());
        }

        @GetMapping("/{eventId}/interest-status")
        public ResponseEntity<Boolean> isInterested(
                        @PathVariable Long eventId,
                        Authentication authentication) {

                return ResponseEntity.ok(
                                eventService.isInterested(
                                                eventId,
                                                authentication.getName()));
        }

        @GetMapping("/{eventId}/sessionsCount")
        public ResponseEntity<Long> getSessionsCount(
                        @PathVariable Long eventId) {

                return ResponseEntity.ok(
                                eventService.getSessionsCount(eventId));
        }

        @GetMapping("/organizer/{organizerId}")
        public ResponseEntity<List<EventSummaryResponse>> getEventsByOrganizer(
                        @PathVariable Long organizerId) {

                return ResponseEntity.ok(
                                eventService.getEventsByOrganizer(
                                                organizerId));
        }

        @GetMapping("/filtered")
        public ResponseEntity<List<EventSummaryResponse>> filterEvents(

                        @RequestParam(required = false) String category,

                        @RequestParam(required = false) String city,

                        @RequestParam(required = false) String keyword) {

                return ResponseEntity.ok(
                                eventService.filterEvents(
                                                category,
                                                city,
                                                keyword));
        }
}
