
package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventScore;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.dto.LocationResponse;
import com.eventhub.backend.dto.OrganizerSummaryResponse;
import com.eventhub.backend.dto.PaginatedResponse;
import com.eventhub.backend.dto.SessionResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.Session;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
                                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<EventSummaryResponse> getEventsByCategory(String category) {

                return eventRepository
                                .findByCategory(category)
                                .stream()
                                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<EventSummaryResponse> searchEvents(String keyword) {

                return eventRepository
                                .findByTitleContainingIgnoreCase(
                                                keyword)
                                .stream()
                                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<String> getCategories() {

                return eventRepository.findDistinctCategories();
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
        public List<EventSummaryResponse> getMyEventsSummary() {

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
                                .map(this::mapToSummary)
                                .toList();
        }

        @Override
        public List<EventSummaryResponse> getSimilarEvents(
                        Long eventId) {

                Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Event not found"));

                return eventRepository.findSimilarEvents(
                                event.getId(),
                                event.getCategory(),
                                event.getOrganizer().getId())
                                .stream()
                                .filter(e -> e.getStatus() != EventStatus.COMPLETED)
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
                                .findByEmailWithInterests(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                Set<Event> interestedEvents = user.getInterestedEvents();

                if (interestedEvents == null
                                || interestedEvents.isEmpty()) {
                        // Return trending events as fallback for users with no interests
                        return getTrendingEvents();
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

                List<EventSummaryResponse> recommended = eventRepository

                                .findRecommendedEvents(

                                                categories,

                                                organizerIds,

                                                interactedEventIds)

                                .stream()

                                .limit(10)

                                .map(this::mapToSummary)

                                .toList();

                return recommended;
        }

        @Override
        public PaginatedResponse<EventSummaryResponse> getMyInterestedEvents(Integer page, Integer limit,
                        String status) {

                String email = SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName();

                User user = userRepository
                                .findByEmailWithInterests(email)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found"));

                Set<Event> interestedEvents = user.getInterestedEvents();

                if (interestedEvents == null
                                || interestedEvents.isEmpty()) {
                        return PaginatedResponse.<EventSummaryResponse>builder()
                                        .data(new ArrayList<>())
                                        .totalPages(0)
                                        .totalItems(0)
                                        .currentPage(page != null ? page : 1)
                                        .limit(limit != null ? limit : 8)
                                        .build();
                }

                List<Event> events = new ArrayList<>(interestedEvents);
                long totalItems = events.size();

                // Filter by status if provided, otherwise exclude COMPLETED events
                if (status != null && !status.isEmpty()) {
                        events = events.stream()
                                        .filter(event -> {
                                                EventStatus eventStatus = event.getStatus();
                                                if (eventStatus == null) {
                                                        return false;
                                                }
                                                return eventStatus.name().equalsIgnoreCase(status);
                                        })
                                        .toList();
                } else {
                        // Exclude COMPLETED events by default
                        events = events.stream()
                                        .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                                        .toList();
                }
                totalItems = events.size();

                int currentPage = page != null ? page : 1;
                int pageSize = limit != null ? limit : 8;
                int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                // Apply pagination if provided
                if (page != null && limit != null) {
                        int startIndex = (page - 1) * limit;
                        if (startIndex >= events.size()) {
                                return PaginatedResponse.<EventSummaryResponse>builder()
                                                .data(new ArrayList<>())
                                                .totalPages(totalPages)
                                                .totalItems(totalItems)
                                                .currentPage(currentPage)
                                                .limit(pageSize)
                                                .build();
                        }
                        int endIndex = Math.min(startIndex + limit, events.size());
                        events = events.subList(startIndex, endIndex);
                }

                List<EventSummaryResponse> data = events.stream()
                                .map(this::mapToSummary)
                                .toList();

                return PaginatedResponse.<EventSummaryResponse>builder()
                                .data(data)
                                .totalPages(totalPages)
                                .totalItems(totalItems)
                                .currentPage(currentPage)
                                .limit(pageSize)
                                .build();
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

                event.setInterestedUsers(
                                (event.getInterestedUsers() == null
                                                ? 0
                                                : event.getInterestedUsers()) + 1);

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

                event.setInterestedUsers(
                                Math.max(
                                                0,
                                                (event.getInterestedUsers() == null
                                                                ? 0
                                                                : event.getInterestedUsers()) - 1));

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

                response.setStartingPrice(event.getStartingPrice() != null
                                ? event.getStartingPrice()
                                : 0.0);

                response.setRecurrence(event.getRecurrence() != null
                                ? event.getRecurrence()
                                : "ONCE");
                LocationResponse location = new LocationResponse();

                location.setLabel(
                                event.getVenue().getCity());

                location.setCoordinates(
                                List.of(
                                                event.getVenue().getLongitude(),
                                                event.getVenue().getLatitude()));
                location.setType("Point");
                response.setAverageRating(
                                event.getAverageRating() == null
                                                ? 0.0
                                                : event.getAverageRating());

                response.setInterestedUsers(
                                event.getInterestedUsers() == null
                                                ? 0
                                                : event.getInterestedUsers());

                response.setLocation(location);
                response.setStartDate(event.getStartDate() != null
                                ? event.getStartDate().toString()
                                : null);
                response.setEndDate(event.getEndDate() != null
                                ? event.getEndDate().toString()
                                : null);
                response.setStartTime(event.getStartTime() != null
                                ? event.getStartTime().toString()
                                : null);

                return response;
        }

        @Override
        public List<EventSummaryResponse> getTrendingEvents() {

                List<EventScore> scores = eventRepository.findAll()
                                .stream()
                                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
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

                if (user.getPreferredLocationLatitude() == null
                                || user.getPreferredLocationLongitude() == null) {

                        return eventRepository.findAll()
                                        .stream()
                                        .filter(event -> event.getStatus() != EventStatus.COMPLETED)
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
                                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                                .filter(event -> event.getVenue() != null
                                                && event.getVenue().getLatitude() != null
                                                && event.getVenue().getLongitude() != null)
                                .filter(event -> {

                                        double distance = calculateDistance(
                                                        user.getPreferredLocationLatitude(),
                                                        user.getPreferredLocationLongitude(),
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
        public PaginatedResponse<EventSummaryResponse> getEventsByOrganizer(Long organizerId, Integer page,
                        Integer limit) {
                int currentPage = (page != null && page > 0) ? page : 1;
                int pageSize = (limit != null && limit > 0) ? limit : 8;

                List<EventSummaryResponse> organizerEvents = eventRepository
                                .findByOrganizerId(organizerId)
                                .stream()
                                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                                .map(this::mapToSummary)
                                .toList();

                int totalItems = organizerEvents.size();
                int totalPages = (int) Math.ceil((double) totalItems / pageSize);
                int startIndex = (currentPage - 1) * pageSize;

                List<EventSummaryResponse> paginatedEvents = organizerEvents.stream()
                                .skip(startIndex)
                                .limit(pageSize)
                                .toList();

                return PaginatedResponse.<EventSummaryResponse>builder()
                                .data(paginatedEvents)
                                .currentPage(currentPage)
                                .totalPages(totalPages)
                                .totalItems(totalItems)
                                .limit(pageSize)
                                .build();
        }

        @Override
        public PaginatedResponse<EventSummaryResponse> filterEvents(
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
                        String endTime) {

                String keywordPattern = keyword != null ? "%" + keyword.toLowerCase() + "%" : null;
                String lowerCity = city != null ? city.toLowerCase() : null;
                String categoriesStr = categories != null ? String.join(",", categories) : null;
                String languagesStr = languages != null ? String.join(",", languages) : null;
                List<Event> events = eventRepository
                                .filterEvents(
                                                categoriesStr,
                                                lowerCity,
                                                keywordPattern,
                                                isFeatured,
                                                recurrence,
                                                minRating,
                                                startDate,
                                                endDate,
                                                languagesStr,
                                                ageLimit,
                                                startTime,
                                                endTime);

                // Exclude completed events
                events = events.stream()
                                .filter(event -> event.getStatus() != EventStatus.COMPLETED)
                                .toList();

                if (Boolean.TRUE.equals(location)) {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                                        && !auth.getName().equals("anonymousUser")) {
                                Optional<User> userOptional = userRepository.findByEmail(auth.getName());
                                if (userOptional.isPresent()) {
                                        User user = userOptional.get();
                                        if (user.getPreferredLocationLatitude() != null
                                                        && user.getPreferredLocationLongitude() != null) {
                                                events = events.stream()
                                                                .filter(event -> event.getVenue() != null
                                                                                && event.getVenue()
                                                                                                .getLatitude() != null
                                                                                && event.getVenue()
                                                                                                .getLongitude() != null)
                                                                .filter(event -> calculateDistance(
                                                                                user.getPreferredLocationLatitude(),
                                                                                user.getPreferredLocationLongitude(),
                                                                                event.getVenue().getLatitude(),
                                                                                event.getVenue().getLongitude()) <= 10)
                                                                .toList();
                                        }
                                }
                        }
                }

                int totalItems = events.size();
                int currentPage = page != null ? page : 1;
                int pageSize = limit != null ? limit : 8;
                int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                int startIndex = (currentPage - 1) * pageSize;

                List<EventSummaryResponse> pagedEvents = events
                                .stream()
                                .skip(startIndex)
                                .limit(pageSize)
                                .map(this::mapToSummary)
                                .toList();

                return PaginatedResponse.<EventSummaryResponse>builder()
                                .data(pagedEvents)
                                .currentPage(currentPage)
                                .totalPages(totalPages)
                                .totalItems(totalItems)
                                .limit(pageSize)
                                .build();
        }

        private EventResponse mapToResponse(Event event) {
                LocationResponse location = new LocationResponse();

                location.setLabel(
                                event.getVenue().getCity());

                location.setCoordinates(
                                List.of(
                                                event.getVenue().getLongitude(),
                                                event.getVenue().getLatitude()));
                location.setType("Point");

                OrganizerSummaryResponse organizerResponse = OrganizerSummaryResponse.builder()
                                ._id(event.getOrganizer().getId())
                                .id(event.getOrganizer().getId())
                                .orgName(
                                                event.getOrganizer()
                                                                .getOrgName())
                                .orgEmail(
                                                event.getOrganizer()
                                                                .getOrgEmail())
                                .orgDescription(
                                                event.getOrganizer()
                                                                .getOrgDescription())
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
                                .venue(event.getVenue().getId())
                                .averageRating(
                                                event.getAverageRating())

                                .interestedUsers(
                                                event.getInterestedUsers())
                                .location(location)
                                .ratings(List.of())
                                .startingPrice(event.getStartingPrice() != null ? event.getStartingPrice() : 0.0)
                                .startDate(event.getStartDate())
                                .endDate(event.getEndDate())
                                .startTime(event.getStartTime())
                                .endTime(event.getEndTime())
                                .recurrence(event.getRecurrence())
                                .thumbnailImage(
                                                event.getBannerImage())
                                .sessions(mapSessions(event))
                                .build();
        }

        private List<SessionResponse> mapSessions(Event event) {
                List<Session> sessions = sessionRepository.findByEvent(event);
                if (sessions == null || sessions.isEmpty()) {
                        return List.of();
                }
                return sessions.stream()
                                .map(session -> SessionResponse.builder()
                                                ._id(session.getId())
                                                .id(session.getId())
                                                .date(session.getDate())
                                                .startTime(session.getStartTime())
                                                .endTime(session.getEndTime())
                                                .releaseDate(session.getReleaseDate())
                                                .event(event.getId())
                                                .tickets(session.getTickets() != null ? session.getTickets().stream()
                                                                .map(t -> {
                                                                        SessionResponse.Ticket ticket = new SessionResponse.Ticket();
                                                                        ticket.setType(t.getType());
                                                                        ticket.setPrice(t.getPrice());
                                                                        ticket.setAvailable(t.getAvailable());
                                                                        ticket.setTotalSeats(t.getTotalSeats());
                                                                        return ticket;
                                                                })
                                                                .toList()
                                                                : List.of())
                                                .occupancy(session.getOccupancy())
                                                .build())
                                .toList();
        }
}
