
package com.eventhub.backend.service.impl;

import com.eventhub.backend.dto.EventRequest;
import com.eventhub.backend.dto.EventResponse;
import com.eventhub.backend.dto.EventScore;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.dto.LocationResponse;
import com.eventhub.backend.dto.OrganizerSummaryResponse;
import com.eventhub.backend.dto.PaginatedResponse;
import com.eventhub.backend.dto.SessionResponse;
import com.eventhub.backend.dto.TicketRequest;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.Session;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.entity.Seat;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.exception.ResourceNotFoundException;
import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.repository.SessionRepository;
import com.eventhub.backend.repository.UserRepository;
import com.eventhub.backend.repository.VenueRepository;
import com.eventhub.backend.service.EventService;
import com.eventhub.backend.util.EventStatusUtil;
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
        private final com.eventhub.backend.repository.SeatRepository seatRepository;

        public EventServiceImpl(
                        EventRepository eventRepository,
                        OrganizerRepository organizerRepository,
                        VenueRepository venueRepository,
                        UserRepository userRepository,
                        BookingRepository bookingRepository,
                        SessionRepository sessionRepository,
                        com.eventhub.backend.repository.SeatRepository seatRepository) {

                this.eventRepository = eventRepository;
                this.organizerRepository = organizerRepository;
                this.venueRepository = venueRepository;
                this.userRepository = userRepository;
                this.bookingRepository = bookingRepository;
                this.sessionRepository = sessionRepository;
                this.seatRepository = seatRepository;
        }

        @Override
        public EventResponse createEvent(EventRequest request) {

                // Validate that venue/venueId is provided
                if (request.getVenueId() == null) {
                        throw new IllegalArgumentException("Venue ID must not be null");
                }

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

                // Calculate starting price
                Double startingPrice = 0.0;
                if (request.getTickets() != null && !request.getTickets().isEmpty()) {
                        startingPrice = request.getTickets().stream()
                                        .mapToDouble(t -> t.getPrice() != null ? t.getPrice() : 0.0)
                                        .min()
                                        .orElse(0.0);
                }

                // Create Event
                Event event = new Event();
                event.setTitle(request.getTitle());
                event.setDescription(request.getDescription());
                event.setCategory(request.getCategory());
                event.setBannerImage(request.getBannerImage());
                event.setThumbnailImage(request.getThumbnailImage());
                event.setOrganizer(organizer);
                event.setVenue(venue);
                event.setStartingPrice(startingPrice);
                event.setRecurrence(request.getRecurrence() != null ? request.getRecurrence() : "single");
                event.setSelectedWeekdays(request.getSelectedWeekdays());
                event.setAgeLimit(request.getAgeLimit());
                event.setLanguages(request.getLanguages());
                event.setStatus(EventStatus.UPCOMING);

                // Parse dates
                if (request.getStartDate() != null) {
                        if (request.getStartDate().contains("T")) {
                                event.setStartDate(java.time.Instant.parse(request.getStartDate())
                                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                        } else {
                                event.setStartDate(java.time.LocalDate.parse(request.getStartDate()));
                        }
                }
                if (request.getEndDate() != null) {
                        if (request.getEndDate().contains("T")) {
                                event.setEndDate(java.time.Instant.parse(request.getEndDate())
                                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                        } else {
                                event.setEndDate(java.time.LocalDate.parse(request.getEndDate()));
                        }
                }
                event.setStartTime(request.getStartTime());
                event.setEndTime(request.getEndTime());

                // Set location
                if (request.getLocation() != null) {
                        event.setLocationType(request.getLocation().getType());
                        if (request.getLocation().getCoordinates() != null
                                        && request.getLocation().getCoordinates().size() >= 2) {
                                event.setLocationLongitude(request.getLocation().getCoordinates().get(0));
                                event.setLocationLatitude(request.getLocation().getCoordinates().get(1));
                        }
                        event.setLocationLabel(request.getLocation().getLabel());
                } else if (venue.getLatitude() != null && venue.getLongitude() != null) {
                        // Fallback to venue location
                        event.setLocationType("Point");
                        event.setLocationLongitude(venue.getLongitude());
                        event.setLocationLatitude(venue.getLatitude());
                        event.setLocationLabel(venue.getCity());
                }

                event = eventRepository.save(event);

                // Add event to organizer's eventsHosted
                if (organizer.getEventsHosted() == null) {
                        organizer.setEventsHosted(new java.util.HashSet<>());
                }
                organizer.getEventsHosted().add(event);
                organizerRepository.save(organizer);

                // Generate sessions
                if (request.getTickets() != null && !request.getTickets().isEmpty()) {
                        List<Session> sessions = generateSessions(event, request, venue);
                        sessionRepository.saveAll(sessions);
                }

                return mapToResponse(event);
        }

        private List<Session> generateSessions(Event event, EventRequest request, Venue venue) {
                List<Session> sessions = new java.util.ArrayList<>();
                java.time.LocalDate startDate = event.getStartDate();
                java.time.LocalDate endDate = event.getEndDate() != null ? event.getEndDate() : startDate;
                String recurrence = request.getRecurrence() != null ? request.getRecurrence() : "single";
                Set<String> selectedWeekdays = request.getSelectedWeekdays() != null ? request.getSelectedWeekdays()
                                : new java.util.HashSet<>();

                // Generate dates based on recurrence
                List<java.time.LocalDate> dates = new java.util.ArrayList<>();
                java.time.LocalDate current = startDate;

                while (!current.isAfter(endDate)) {
                        boolean shouldAdd = false;
                        if ("single".equals(recurrence)) {
                                shouldAdd = true;
                        } else if ("multi-day".equals(recurrence)) {
                                shouldAdd = true;
                        } else if ("weekly".equals(recurrence)) {
                                // Check if current weekday is in selectedWeekdays
                                java.time.DayOfWeek dayOfWeek = current.getDayOfWeek();
                                String dayStr = dayOfWeek.toString().substring(0, 3).toUpperCase(); // MON, TUE, etc.
                                // Also check for lowercase or other formats
                                if (selectedWeekdays.contains(dayStr) ||
                                                selectedWeekdays.contains(dayStr.toLowerCase()) ||
                                                selectedWeekdays.contains(dayStr.substring(0, 1).toUpperCase()
                                                                + dayStr.substring(1).toLowerCase())) {
                                        shouldAdd = true;
                                }
                        }

                        if (shouldAdd) {
                                dates.add(current);
                        }

                        if ("single".equals(recurrence)) {
                                break;
                        }
                        current = current.plusDays(1);
                }

                // Generate seats from venue seating layout (once)
                List<Seat> baseSeats = generateSeatsFromLayout(venue.getSeatingLayout());

                // Calculate release date
                java.time.LocalDateTime creationDate = java.time.LocalDateTime.now();
                java.time.LocalDateTime idealFirstRelease = startDate.atStartOfDay().minusDays(20);
                java.time.LocalDateTime releaseDateBase = idealFirstRelease.isAfter(creationDate) ? idealFirstRelease
                                : creationDate;

                // Create sessions for each date
                for (int i = 0; i < dates.size(); i++) {
                        java.time.LocalDate date = dates.get(i);
                        Session session = new Session();
                        session.setDate(date);
                        session.setStartTime(request.getStartTime());
                        session.setEndTime(request.getEndTime());
                        session.setEvent(event);
                        session.setOccupancy(0);

                        // Set release date
                        java.time.LocalDateTime releaseDate = releaseDateBase.plusDays((i / 7) * 7);
                        session.setReleaseDate(releaseDate);

                        // Build tickets
                        List<Session.Ticket> tickets = new java.util.ArrayList<>();
                        if (request.getTickets() != null) {
                                for (TicketRequest ticketReq : request.getTickets()) {
                                        Session.Ticket ticket = new Session.Ticket();
                                        String type = ticketReq.getType() != null ? ticketReq.getType()
                                                        : ticketReq.getSection();
                                        ticket.setType(type);
                                        ticket.setPrice(ticketReq.getPrice());
                                        Integer available = ticketReq.getAvailable() != null ? ticketReq.getAvailable()
                                                        : ticketReq.getCapacity();
                                        Integer totalSeats = ticketReq.getTotalSeats() != null
                                                        ? ticketReq.getTotalSeats()
                                                        : ticketReq.getCapacity();
                                        ticket.setAvailable(available);
                                        ticket.setTotalSeats(totalSeats);
                                        tickets.add(ticket);
                                }
                        }
                        session.setTickets(tickets);

                        // Save session first to get ID
                        session = sessionRepository.save(session);

                        // Create seats for this session
                        List<Seat> sessionSeats = new java.util.ArrayList<>();
                        for (Seat baseSeat : baseSeats) {
                                Seat seat = new Seat();
                                seat.setSeatId(baseSeat.getSeatId());
                                seat.setSection(baseSeat.getSection());
                                seat.setStatus(com.eventhub.backend.enums.SeatStatus.AVAILABLE);
                                seat.setSession(session);
                                sessionSeats.add(seat);
                        }
                        if (!sessionSeats.isEmpty()) {
                                seatRepository.saveAll(sessionSeats);
                        }

                        sessions.add(session);
                }

                return sessions;
        }

        private List<Seat> generateSeatsFromLayout(String seatingLayoutJson) {
                List<Seat> seats = new java.util.ArrayList<>();
                if (seatingLayoutJson == null || seatingLayoutJson.isBlank()) {
                        return seats;
                }

                try {
                        // Parse JSON seating layout
                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        List<java.util.Map<String, Object>> layout = objectMapper.readValue(seatingLayoutJson,
                                        List.class);

                        for (java.util.Map<String, Object> section : layout) {
                                String sectionName = (String) section.get("section");
                                List<java.util.Map<String, Object>> rows = (List<java.util.Map<String, Object>>) section
                                                .get("rows");

                                if (rows != null && !rows.isEmpty()) {
                                        for (java.util.Map<String, Object> row : rows) {
                                                List<String> rowSeats = (List<String>) row.get("seats");
                                                if (rowSeats != null && !rowSeats.isEmpty()) {
                                                        for (String seatId : rowSeats) {
                                                                Seat seat = new Seat();
                                                                seat.setSeatId(seatId);
                                                                seat.setSection(sectionName);
                                                                seats.add(seat);
                                                        }
                                                } else {
                                                        // Fallback: generate seats evenly
                                                        Integer sectionCapacity = (Integer) section
                                                                        .get("sectionCapacity");
                                                        String rowLabel = (String) row.get("label");
                                                        if (sectionCapacity != null && rowLabel != null) {
                                                                int seatsPerRow = sectionCapacity / rows.size();
                                                                for (int i = 1; i <= seatsPerRow; i++) {
                                                                        Seat seat = new Seat();
                                                                        seat.setSeatId(rowLabel + i);
                                                                        seat.setSection(sectionName);
                                                                        seats.add(seat);
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                } catch (Exception e) {
                        System.err.println("Error parsing seating layout: " + e.getMessage());
                }

                return seats;
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

                return eventRepository.findAllExcludingStatus(EventStatus.COMPLETED)
                                .stream()
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<EventSummaryResponse> getEventsByCategory(String category) {

                return eventRepository
                                .findByCategoryExcludingStatus(category, EventStatus.COMPLETED)
                                .stream()
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<EventSummaryResponse> searchEvents(String keyword) {

                return eventRepository
                                .findByTitleContainingIgnoreCaseExcludingStatus(keyword, EventStatus.COMPLETED)
                                .stream()
                                .map(this::mapToSummary)
                                .collect(Collectors.toList());
        }

        @Override
        public List<String> getCategories() {
                // Predefined categories matching Node.js
                return List.of(
                                "Music",
                                "Sports",
                                "Workshops",
                                "Conferences",
                                "Festivals",
                                "Tech & Innovation",
                                "Charity",
                                "Comedy",
                                "Exhibitions");
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
                        // Return empty list if no recommendations
                        return new ArrayList<>();
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

                int currentPage = page != null ? page : 1;
                int pageSize = limit != null ? limit : 8;

                // Use proper database pagination instead of in-memory pagination
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                                .of(currentPage - 1, pageSize);

                EventStatus statusFilter = null;
                if (status != null && !status.isEmpty()) {
                        try {
                                statusFilter = EventStatus.valueOf(status.toUpperCase());
                        } catch (IllegalArgumentException e) {
                                // Invalid status, default to excluding COMPLETED
                        }
                }

                org.springframework.data.domain.Page<Event> eventPage = userRepository
                                .findInterestedEventsByEmail(email, statusFilter, pageable);

                List<EventSummaryResponse> data = eventPage.getContent()
                                .stream()
                                .map(this::mapToSummary)
                                .toList();

                return PaginatedResponse.<EventSummaryResponse>builder()
                                .data(data)
                                .totalPages(eventPage.getTotalPages())
                                .totalItems(eventPage.getTotalElements())
                                .currentPage(currentPage)
                                .limit(pageSize)
                                .build();
        }

        @Override
        public Integer markInterest(
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
                        return event.getInterestedUsers() == null ? 0 : event.getInterestedUsers();
                }

                user.getInterestedEvents()
                                .add(event);

                event.setInterestedUsers(
                                (event.getInterestedUsers() == null
                                                ? 0
                                                : event.getInterestedUsers()) + 1);

                userRepository.save(user);
                eventRepository.save(event);
                return event.getInterestedUsers();
        }

        @Override
        public Integer removeInterest(
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
                return event.getInterestedUsers();
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
                event.setThumbnailImage(request.getThumbnailImage());
                event.setVenue(venue);
                event.setAgeLimit(request.getAgeLimit());
                if (request.getLanguages() != null) {
                        event.setLanguages(request.getLanguages());
                }
                if (request.getSelectedWeekdays() != null) {
                        event.setSelectedWeekdays(request.getSelectedWeekdays());
                }
                if (request.getStartDate() != null) {
                        if (request.getStartDate().contains("T")) {
                                event.setStartDate(java.time.Instant.parse(request.getStartDate())
                                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                        } else {
                                event.setStartDate(java.time.LocalDate.parse(request.getStartDate()));
                        }
                }
                if (request.getEndDate() != null) {
                        if (request.getEndDate().contains("T")) {
                                event.setEndDate(java.time.Instant.parse(request.getEndDate())
                                                .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
                        } else {
                                event.setEndDate(java.time.LocalDate.parse(request.getEndDate()));
                        }
                }
                if (request.getStartTime() != null) {
                        event.setStartTime(request.getStartTime());
                }
                if (request.getEndTime() != null) {
                        event.setEndTime(request.getEndTime());
                }
                if (request.getRecurrence() != null) {
                        event.setRecurrence(request.getRecurrence());
                }

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
        public EventResponse updateEventImage(Long id, java.util.Map<String, String> request) {
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

                if (request.containsKey("bannerImage")) {
                        event.setBannerImage(request.get("bannerImage"));
                }
                if (request.containsKey("thumbnailImage")) {
                        event.setThumbnailImage(request.get("thumbnailImage"));
                }

                eventRepository.save(event);
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

                // Delete associated sessions first
                sessionRepository.deleteAll(sessionRepository.findByEvent(event));

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
                                EventStatusUtil.getCurrentStatus(event).name());
                response.setThumbnailImage(
                                event.getThumbnailImage()); // temporary

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

                // Use optimized query with database-level LIMIT
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                                10);
                List<Event> events = eventRepository.findTrendingEventsNative(EventStatus.COMPLETED, pageable);

                return events.stream()
                                .map(this::mapToSummary)
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

                Double userLat = user.getPreferredLocationLatitude();
                Double userLon = user.getPreferredLocationLongitude();

                // Use optimized database query instead of in-memory filtering
                List<Event> popularEvents = eventRepository.findPopularEvents(userLat, userLon, 10);

                return popularEvents.stream()
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

                // Use proper database pagination instead of in-memory pagination
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                                .of(currentPage - 1, pageSize);
                org.springframework.data.domain.Page<Event> eventPage = eventRepository
                                .findByOrganizerIdAndStatusNot(organizerId, EventStatus.COMPLETED, pageable);

                List<EventSummaryResponse> paginatedEvents = eventPage.getContent()
                                .stream()
                                .map(this::mapToSummary)
                                .toList();

                return PaginatedResponse.<EventSummaryResponse>builder()
                                .data(paginatedEvents)
                                .currentPage(currentPage)
                                .totalPages(eventPage.getTotalPages())
                                .totalItems(eventPage.getTotalElements())
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

                int currentPage = page != null ? page : 1;
                int pageSize = limit != null ? limit : 8;
                int offset = (currentPage - 1) * pageSize;

                Double userLat = null;
                Double userLon = null;

                if (Boolean.TRUE.equals(location)) {
                        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                                        && !auth.getName().equals("anonymousUser")) {
                                Optional<User> userOptional = userRepository.findByEmail(auth.getName());
                                if (userOptional.isPresent()) {
                                        User user = userOptional.get();
                                        userLat = user.getPreferredLocationLatitude();
                                        userLon = user.getPreferredLocationLongitude();
                                }
                        }
                }

                // Use optimized paginated query instead of in-memory filtering
                List<Event> events = eventRepository.filterEventsPaginated(
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
                                endTime,
                                userLat,
                                userLon,
                                pageSize,
                                offset);

                // Get total count for pagination
                Long totalItems = eventRepository.countFilterEvents(
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
                                endTime,
                                userLat,
                                userLon);

                int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                List<EventSummaryResponse> pagedEvents = events.stream()
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
                                .status(EventStatusUtil.getCurrentStatus(event) != null
                                                ? EventStatusUtil.getCurrentStatus(event)
                                                                .name()
                                                : null)
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
                                                event.getThumbnailImage())
                                // Set fields we were missing!
                                .ageLimit(event.getAgeLimit())
                                .languages(event.getLanguages() != null ? new ArrayList<>(event.getLanguages())
                                                : new ArrayList<>())
                                .selectedWeekdays(event.getSelectedWeekdays() != null
                                                ? new ArrayList<>(event.getSelectedWeekdays())
                                                : new ArrayList<>())
                                .isFeatured(event.getIsFeatured())
                                .createdAt(event.getCreatedAt())
                                .updatedAt(event.getUpdatedAt())
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
