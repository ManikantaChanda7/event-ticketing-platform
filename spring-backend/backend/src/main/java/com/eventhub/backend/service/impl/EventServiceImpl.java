
package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventScore;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.dto.LocationResponse;
import com.eventhub.backend.dto.OrganizerSummaryResponse;
import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.repository.SessionRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.repository.VenueRepository;
import com.eventhub.backend.service.EventService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

        private final EventRepository eventRepository;
        private final OrganizerRepository organizerRepository;
        private final VenueRepository venueRepository;
        private final UserRepository userRepository;
        private final BookingRepository bookingRepository;
        private final SessionRepository sessionRepository;

        public EventServiceImpl(
                        EventRepository eventRepository,
                        OrganizerRepository organizerRepository,
                        VenueRepository venueRepository,
                        UserRepository userRepository,
                        BookingRepository bookingRepository,
                        SessionRepository sessionRepository) {

                this.eventRepository = eventRepository;
                this.organizerRepository = organizerRepository;
                this.venueRepository = venueRepository;
                this.userRepository = userRepository;
                this.bookingRepository = bookingRepository;
                this.sessionRepository = sessionRepository;
        }

        @Override
        public EventResponse createEvent(EventRequest request) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Organizer organizer = organizerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

                Venue venue = venueRepository.findById(request.getVenueId())
                                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

                Event event = new Event();
                event.setTitle(request.getTitle());
                event.setDescription(request.getDescription());
                event.setCategory(request.getCategory());
                event.setBannerImage(request.getBannerImage());
                event.setOrganizer(organizer);
                event.setVenue(venue);

                if (request.getStatus() != null && !request.getStatus().isBlank()) {
                        event.setStatus(EventStatus.valueOf(request.getStatus().toUpperCase()));
                }

                event = eventRepository.save(event);

                return mapToResponse(event);
        }

        // @Override
        // public List<EventResponse> getAllEvents() {
        // return eventRepository.findAll()
        // .stream()
        // .map(this::mapToResponse)
        // .toList();
        // }

        @Override
        public List<EventSummaryResponse> getAllEvents() {

                return eventRepository.findAll()
                                .stream()
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<EventSummaryResponse> getEventsByCategory(String category) {

                return eventRepository
                                .findByCategory(category)
                                .stream()
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<EventSummaryResponse> searchEvents(String keyword) {

                return eventRepository
                                .findByTitleContainingIgnoreCase(
                                                keyword)
                                .stream()
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<String> getCategories() {

                return eventRepository.findAll()
                                .stream()
                                .map(Event::getCategory)
                                .distinct()
                                .toList();
        }

        @Override
        public EventResponse getEventById(Long id) {

                Event event = eventRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                return mapToResponse(event);
        }

        @Override
        public List<EventResponse> getMyEvents() {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Organizer organizer = organizerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

                return eventRepository.findByOrganizer(organizer)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Override
        public List<EventSummaryResponse> getSimilarEvents(
                        Long eventId) {

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found"));

                return eventRepository
                                .findTop5ByCategoryAndIdNot(
                                                event.getCategory(),
                                                event.getId())
                                .stream()
                                .map(this::mapToSummary)
                                .toList();
        }

        @Override
        public List<EventSummaryResponse> getRecommendedEvents() {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                Set<Event> interestedEvents = user.getInterestedEvents();

                if (interestedEvents == null
                                || interestedEvents.isEmpty()) {

                        return List.of();
                }

                List<Long> interactedEventIds = interestedEvents.stream()
                                .map(Event::getId)
                                .toList();

                List<String> categories = interestedEvents.stream()
                                .map(Event::getCategory)
                                .distinct()
                                .toList();

                List<Long> organizerIds = interestedEvents.stream()
                                .map(e -> e.getOrganizer().getId())
                                .distinct()
                                .toList();

                return eventRepository.findAll()
                                .stream()
                                .filter(event -> event.getStatus() == EventStatus.PUBLISHED)

                                .filter(event -> !interactedEventIds.contains(
                                                event.getId()))

                                .filter(event -> categories.contains(
                                                event.getCategory())
                                                ||
                                                organizerIds.contains(
                                                                event.getOrganizer()
                                                                                .getId()))

                                .sorted(
                                                Comparator.comparing(
                                                                Event::getInterestedUsers,
                                                                Comparator.nullsLast(
                                                                                Integer::compareTo))
                                                                .reversed())

                                .limit(10)

                                .map(this::mapToSummary)
                                .toList();
        }

        @Override
        public void markInterest(
                        Long eventId,
                        String userEmail) {

                User user = userRepository
                                .findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                Event event = eventRepository
                                .findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found"));

                if (user.getInterestedEvents()
                                .contains(event)) {
                        return;
                }

                user.getInterestedEvents()
                                .add(event);

                long interestedCount = userRepository.findAll()
                                .stream()
                                .filter(u -> u.getInterestedEvents()
                                                .contains(event))
                                .count();

                event.setInterestedUsers(
                                (int) interestedCount);

                userRepository.save(user);
                eventRepository.save(event);
        }

        @Override
        public void removeInterest(
                        Long eventId,
                        String userEmail) {

                User user = userRepository
                                .findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                Event event = eventRepository
                                .findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found"));

                user.getInterestedEvents()
                                .remove(event);

                long interestedCount = userRepository.findAll()
                                .stream()
                                .filter(u -> u.getInterestedEvents()
                                                .contains(event))
                                .count();

                event.setInterestedUsers(
                                (int) interestedCount);

                userRepository.save(user);
                eventRepository.save(event);
        }

        @Override
        public EventResponse updateEvent(
                        Long id,
                        EventRequest request) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Organizer organizer = organizerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

                Event event = eventRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                // Ownership Validation
                if (!event.getOrganizer().getId()
                                .equals(organizer.getId())) {

                        throw new RuntimeException(
                                        "You are not allowed to update this event");
                }

                Venue venue = venueRepository.findById(request.getVenueId())
                                .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));

                event.setTitle(request.getTitle());
                event.setDescription(request.getDescription());
                event.setCategory(request.getCategory());
                event.setBannerImage(request.getBannerImage());
                event.setVenue(venue);

                if (request.getStatus() != null
                                && !request.getStatus().isBlank()) {

                        event.setStatus(
                                        EventStatus.valueOf(
                                                        request.getStatus().toUpperCase()));
                }

                event = eventRepository.save(event);

                return mapToResponse(event);
        }

        @Override
        public void deleteEvent(Long id) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

                Organizer organizer = organizerRepository.findByUser(user)
                                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

                Event event = eventRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

                if (!event.getOrganizer().getId()
                                .equals(organizer.getId())) {

                        throw new RuntimeException(
                                        "You are not allowed to delete this event");
                }

                eventRepository.delete(event);
        }

        private EventSummaryResponse mapToSummary(
                        Event event) {

                EventSummaryResponse response = new EventSummaryResponse();
                response.set_id(event.getId());
                response.setId(event.getId());
                response.setTitle(event.getTitle());
                response.setCategory(event.getCategory());
                response.setBannerImage(
                                event.getBannerImage());

                response.setStatus(
                                event.getStatus().name());
                response.setThumbnailImage(
                                event.getBannerImage()); // temporary

                response.setStartingPrice(0.0); // temporary

                response.setRecurrence("ONCE"); // temporary
                LocationResponse location = new LocationResponse();

                location.setLabel(
                                event.getVenue().getCity());

                location.setCoordinates(
                                List.of(
                                                event.getVenue().getLongitude(),
                                                event.getVenue().getLatitude()));
                response.setAverageRating(
                                event.getAverageRating() == null
                                                ? 0.0
                                                : event.getAverageRating());

                response.setInterestedUsers(
                                event.getInterestedUsers() == null
                                                ? 0
                                                : event.getInterestedUsers());

                response.setLocation(location);
                response.setStartDate(null);
                response.setEndDate(null);
                response.setStartTime(null);

                return response;
        }

        @Override
        public List<EventSummaryResponse> getTrendingEvents() {

                List<EventScore> scores = eventRepository.findAll()
                                .stream()
                                .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                                .map(event -> {

                                        Long ticketsSold = bookingRepository
                                                        .getTicketsSold(event);

                                        Double averageRating = event.getAverageRating() == null
                                                        ? 0.0
                                                        : event.getAverageRating();

                                        Integer interestedUsers = event.getInterestedUsers() == null
                                                        ? 0
                                                        : event.getInterestedUsers();

                                        double score = (averageRating * 10)
                                                        + interestedUsers
                                                        + ticketsSold;

                                        EventScore eventScore = new EventScore();

                                        eventScore.setEvent(
                                                        mapToSummary(event));

                                        eventScore.setScore(score);

                                        return eventScore;
                                })
                                .sorted(
                                                Comparator.comparing(
                                                                EventScore::getScore)
                                                                .reversed())
                                .limit(10)
                                .toList();

                return scores.stream()
                                .map(EventScore::getEvent)
                                .toList();
        }

        @Override
        public List<EventSummaryResponse> getPopularEvents() {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                if (user.getLatitude() == null
                                || user.getLongitude() == null) {

                        return eventRepository.findAll()
                                        .stream()
                                        .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                                        .sorted(
                                                        Comparator
                                                                        .comparing(
                                                                                        Event::getInterestedUsers,
                                                                                        Comparator.nullsLast(
                                                                                                        Integer::compareTo))
                                                                        .reversed()
                                                                        .thenComparing(
                                                                                        Event::getAverageRating,
                                                                                        Comparator.nullsLast(
                                                                                                        Double::compareTo))
                                                                        .reversed())
                                        .limit(10)
                                        .map(this::mapToSummary)
                                        .toList();
                }

                return eventRepository.findAll()
                                .stream()
                                .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                                .filter(event -> event.getVenue() != null
                                                && event.getVenue().getLatitude() != null
                                                && event.getVenue().getLongitude() != null)
                                .filter(event -> {

                                        double distance = calculateDistance(
                                                        user.getLatitude(),
                                                        user.getLongitude(),
                                                        event.getVenue().getLatitude(),
                                                        event.getVenue().getLongitude());

                                        return distance <= 20;
                                })
                                .sorted(
                                                Comparator
                                                                .comparing(
                                                                                Event::getInterestedUsers,
                                                                                Comparator.nullsLast(
                                                                                                Integer::compareTo))
                                                                .reversed()
                                                                .thenComparing(
                                                                                Event::getAverageRating,
                                                                                Comparator.nullsLast(
                                                                                                Double::compareTo))
                                                                .reversed())
                                .limit(10)
                                .map(this::mapToSummary)
                                .toList();
        }

        private double calculateDistance(
                        double lat1,
                        double lon1,
                        double lat2,
                        double lon2) {

                final int R = 6371;

                double latDistance = Math.toRadians(lat2 - lat1);

                double lonDistance = Math.toRadians(lon2 - lon1);

                double a = Math.sin(latDistance / 2)
                                * Math.sin(latDistance / 2)
                                + Math.cos(Math.toRadians(lat1))
                                                * Math.cos(Math.toRadians(lat2))
                                                * Math.sin(lonDistance / 2)
                                                * Math.sin(lonDistance / 2);

                double c = 2 * Math.atan2(
                                Math.sqrt(a),
                                Math.sqrt(1 - a));

                return R * c;
        }

        @Override
        public List<EventSummaryResponse> getMyInterestedEvents() {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                return user.getInterestedEvents()
                                .stream()
                                .map(this::mapToSummary)
                                .toList();
        }

        @Override
        public boolean isInterested(
                        Long eventId,
                        String userEmail) {

                User user = userRepository
                                .findByEmail(userEmail)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                return user.getInterestedEvents()
                                .stream()
                                .anyMatch(event -> event.getId()
                                                .equals(eventId));
        }

        @Override
        public long getSessionsCount(Long eventId) {

                Event event = eventRepository
                                .findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found"));

                return sessionRepository.countByEvent(event);
        }

        @Override
        public List<EventSummaryResponse> getEventsByOrganizer(Long organizerId) {

                return eventRepository
                                .findByOrganizerId(organizerId)
                                .stream()
                                .map(this::mapToSummary)
                                .toList();
        }

        @Override
        public List<EventSummaryResponse> filterEvents(
                        String category,
                        String city,
                        String keyword) {

                return eventRepository
                                .filterEvents(
                                                category,
                                                city,
                                                keyword)
                                .stream()
                                .map(this::mapToSummary)
                                .toList();
        }

        private EventResponse mapToResponse(Event event) {
                LocationResponse location = new LocationResponse();

                location.setLabel(
                                event.getVenue().getCity());

                location.setCoordinates(
                                List.of(
                                                event.getVenue().getLongitude(),
                                                event.getVenue().getLatitude()));

                OrganizerSummaryResponse organizerResponse = OrganizerSummaryResponse.builder()
                                ._id(event.getOrganizer().getId())
                                .id(event.getOrganizer().getId())
                                .organizationName(
                                                event.getOrganizer()
                                                                .getOrganizationName())
                                .verified(
                                                event.getOrganizer()
                                                                .getVerified())
                                .website(
                                                event.getOrganizer()
                                                                .getWebsite())
                                .build();

                return EventResponse.builder()
                                ._id(event.getId())
                                .id(event.getId())
                                .title(event.getTitle())
                                .description(event.getDescription())
                                .category(event.getCategory())
                                .bannerImage(event.getBannerImage())
                                .status(event.getStatus() != null ? event.getStatus().name() : null)
                                .organizer(organizerResponse)

                                .averageRating(
                                                event.getAverageRating())

                                .interestedUsers(
                                                event.getInterestedUsers())
                                .location(location)
                                .ratings(List.of())
                                .startingPrice(0.0)

                                .thumbnailImage(
                                                event.getBannerImage())
                                .build();
        }
}
