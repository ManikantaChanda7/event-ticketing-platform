package com.eventhub.backend.controller;

import com.eventhub.backend.dto.ApiResponse;
import com.eventhub.backend.dto.EventSummaryResponse;
import com.eventhub.backend.dto.LocationResponse;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.Session;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.repository.BookingRepository;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.repository.SessionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/organizer")
public class OrganizerDashboardController {

    private final OrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final BookingRepository bookingRepository;

    public OrganizerDashboardController(
            OrganizerRepository organizerRepository,
            EventRepository eventRepository,
            SessionRepository sessionRepository,
            BookingRepository bookingRepository) {
        this.organizerRepository = organizerRepository;
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
        this.bookingRepository = bookingRepository;
    }

    private Organizer getOrganizerFromUser(Authentication authentication) {
        String email = authentication.getName();
        return organizerRepository.findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
    }

    private long getBookingTicketCount(com.eventhub.backend.entity.Booking booking) {
        if (booking.getTicketsSummary() != null && !booking.getTicketsSummary().isEmpty()) {
            return booking.getTicketsSummary().stream()
                    .mapToLong(t -> t.getQuantity() != null ? t.getQuantity() : 0)
                    .sum();
        }

        if (booking.getSeats() != null) {
            return booking.getSeats().size();
        }

        return 0;
    }

    private double getBookingAmount(com.eventhub.backend.entity.Booking booking) {
        BigDecimal amount = booking.getTotalAmount();
        return amount != null ? amount.doubleValue() : 0.0;
    }

    @GetMapping("/dashboard/basic")
    public ApiResponse<Map<String, Object>> getBasicStats(Authentication authentication) {
        try {
            Organizer organizer = getOrganizerFromUser(authentication);

            long totalEvents = eventRepository.countByOrganizer(organizer);
            long activeEvents = eventRepository.countByOrganizerAndStatusIn(
                    organizer,
                    List.of(EventStatus.UPCOMING.name(), EventStatus.ONGOING.name()));

            Map<String, Object> response = new HashMap<>();
            response.put("totalEvents", totalEvents);
            response.put("activeEvents", activeEvents);
            response.put("organizer", organizer.getOrgName());

            return new ApiResponse<>(true, "Basic stats fetched successfully", response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("totalEvents", 0);
            response.put("activeEvents", 0);
            response.put("organizer", "");
            return new ApiResponse<>(true, "Basic stats fetched successfully", response);
        }
    }

    @GetMapping("/dashboard/sales")
    public ApiResponse<Map<String, Object>> getSalesStats(Authentication authentication) {
        Organizer organizer = getOrganizerFromUser(authentication);

        List<com.eventhub.backend.entity.Booking> bookings = bookingRepository.findAll();
        long totalTicketsSold = 0;
        double totalRevenue = 0.0;
        for (com.eventhub.backend.entity.Booking booking : bookings) {
            if (booking.getEvent() != null
                    && booking.getEvent().getOrganizer() != null
                    && booking.getEvent().getOrganizer().getId().equals(organizer.getId())) {
                totalTicketsSold += getBookingTicketCount(booking);
                totalRevenue += getBookingAmount(booking);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalTicketsSold", totalTicketsSold);
        response.put("totalRevenue", totalRevenue);

        return new ApiResponse<>(true, "Sales stats fetched successfully", response);
    }

    @GetMapping("/dashboard/categories")
    public ApiResponse<Map<String, Object>> getCategoryStats(Authentication authentication) {
        Organizer organizer = getOrganizerFromUser(authentication);

        List<Event> events = eventRepository.findByOrganizer(organizer);
        long totalEvents = events.size();

        Map<String, Long> categoryCounts = events.stream()
                .collect(Collectors.groupingBy(
                        Event::getCategory,
                        Collectors.counting()));

        List<Map<String, Object>> categoryPercentages = categoryCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> cat = new HashMap<>();
                    cat.put("category", entry.getKey());
                    cat.put("percentage", totalEvents > 0
                            ? String.format("%.2f", (entry.getValue() * 100.0 / totalEvents))
                            : "0.00");
                    return cat;
                })
                .collect(Collectors.toList());

        Map<String, Double> revenueByCategoryMap = new HashMap<>();

        List<com.eventhub.backend.entity.Booking> bookings = bookingRepository.findAll();

        for (com.eventhub.backend.entity.Booking booking : bookings) {
            if (booking.getEvent() != null
                    && booking.getEvent().getOrganizer() != null
                    && booking.getEvent().getOrganizer().getId().equals(organizer.getId())) {

                String category = booking.getEvent().getCategory();
                revenueByCategoryMap.merge(
                        category,
                        getBookingAmount(booking),
                        Double::sum);
            }
        }

        List<Map<String, Object>> revenueByCategory = revenueByCategoryMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> rev = new HashMap<>();
                    rev.put("_id", entry.getKey());
                    rev.put("revenue", entry.getValue());
                    return rev;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("categoryPercentages", categoryPercentages);
        response.put("revenueByCategory", revenueByCategory);

        return new ApiResponse<>(true, "Category stats fetched successfully", response);
    }

    @GetMapping("/dashboard/top-selling")
    public ApiResponse<Map<String, Object>> getTopSellingEvents(Authentication authentication) {
        try {
            Organizer organizer = getOrganizerFromUser(authentication);

            List<com.eventhub.backend.entity.Booking> bookings = bookingRepository.findAll();
            Map<Long, Map<String, Object>> eventStats = new HashMap<>();

            for (com.eventhub.backend.entity.Booking booking : bookings) {
                if (booking.getEvent() == null
                        || booking.getEvent().getOrganizer() == null
                        || !booking.getEvent().getOrganizer().getId().equals(organizer.getId())) {
                    continue;
                }

                Event event = booking.getEvent();
                Long eventId = event.getId();

                eventStats.compute(eventId, (key, existing) -> {
                    if (existing == null) {
                        existing = new HashMap<>();
                        existing.put("title", event.getTitle());
                        existing.put("ticketsSold", getBookingTicketCount(booking));
                        existing.put("revenue", getBookingAmount(booking));
                    } else {
                        existing.put("ticketsSold",
                                ((Number) existing.get("ticketsSold")).longValue() + getBookingTicketCount(booking));
                        existing.put("revenue",
                                ((Number) existing.get("revenue")).doubleValue() + getBookingAmount(booking));
                    }
                    return existing;
                });
            }

            List<Map<String, Object>> topSelling = eventStats.values().stream()
                    .sorted((a, b) -> Long.compare(
                            ((Number) b.get("ticketsSold")).longValue(),
                            ((Number) a.get("ticketsSold")).longValue()))
                    .limit(3)
                    .collect(Collectors.toList());

            Map<String, Object> topSellingData = new HashMap<>();
            topSellingData.put("topSelling", topSelling);

            return new ApiResponse<>(true, "Top selling events fetched successfully", topSellingData);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> topSellingData = new HashMap<>();
            topSellingData.put("topSelling", List.of());
            return new ApiResponse<>(true, "Top selling events fetched successfully", topSellingData);
        }
    }

    @GetMapping("/dashboard/upcoming")
    public ApiResponse<Map<String, Object>> getUpcomingEvents(Authentication authentication) {
        try {
            Organizer organizer = getOrganizerFromUser(authentication);

            List<Event> allEvents = eventRepository.findByOrganizer(organizer);
            List<EventSummaryResponse> upcomingEvents = allEvents.stream()
                    .filter(e -> e.getStatus() == EventStatus.UPCOMING)
                    .sorted((e1, e2) -> e1.getStartDate().compareTo(e2.getStartDate()))
                    .limit(3)
                    .map(this::mapToSummary)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("upcomingEvents", upcomingEvents);

            return new ApiResponse<>(true, "Upcoming events fetched successfully", response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("upcomingEvents", List.of());
            return new ApiResponse<>(true, "Upcoming events fetched successfully", response);
        }
    }

    @GetMapping("/events")
    public ApiResponse<Map<String, Object>> getEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        Organizer organizer = getOrganizerFromUser(authentication);

        List<Event> events = eventRepository.findByOrganizerOrderByCreatedAtDesc(organizer);

        int total = events.size();
        int startIndex = (page - 1) * limit;
        int endIndex = Math.min(startIndex + limit, total);

        List<EventSummaryResponse> paginatedEvents = events.subList(startIndex, endIndex)
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", paginatedEvents);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", total);
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", (int) Math.ceil((double) total / limit));
        response.put("pagination", pagination);

        return new ApiResponse<>(true, "Events fetched successfully", response);
    }

    @GetMapping("/manage-event/{id}")
    public ApiResponse<Map<String, Object>> getManageEventData(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Organizer organizer = getOrganizerFromUser(authentication);

            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // Verify the event belongs to the organizer
            if (!event.getOrganizer().getId().equals(organizer.getId())) {
                return new ApiResponse<>(false, "You don't have permission to manage this event", null);
            }

            // Get sessions for this event
            List<Session> sessions = sessionRepository.findByEvent(event);

            // Map sessions to minimal response to avoid exposing sensitive data
            List<Map<String, Object>> sessionSummaries = sessions.stream()
                    .map(session -> {
                        Map<String, Object> sessionData = new HashMap<>();
                        sessionData.put("sessionId", session.getId());
                        sessionData.put("id", session.getId());
                        sessionData.put("date", session.getDate());
                        sessionData.put("startTime", session.getStartTime());
                        sessionData.put("endTime", session.getEndTime());
                        sessionData.put("occupancy", session.getOccupancy());

                        // Include ticket summary
                        List<Map<String, Object>> ticketSummaries = new ArrayList<>();
                        Map<String, Map<String, Object>> typeStats = new HashMap<>();

                        if (session.getTickets() != null && !session.getTickets().isEmpty()) {
                            ticketSummaries = session.getTickets().stream()
                                    .map(ticket -> {
                                        Map<String, Object> ticketData = new HashMap<>();
                                        ticketData.put("type", ticket.getType());
                                        ticketData.put("price", ticket.getPrice());
                                        ticketData.put("available", ticket.getAvailable());
                                        ticketData.put("totalSeats", ticket.getTotalSeats());
                                        return ticketData;
                                    })
                                    .collect(Collectors.toList());

                            // Add stats.typeStats for frontend compatibility using actual booking data
                            List<Map<String, Object>> sessionBookingStats = bookingRepository
                                    .getTicketTypeStatsBySession(session);
                            Map<String, Integer> soldByType = new HashMap<>();
                            for (Map<String, Object> stat : sessionBookingStats) {
                                String type = (String) stat.get("type");
                                Long sold = ((Number) stat.get("sold")).longValue();
                                soldByType.put(type, sold.intValue());
                            }

                            for (Session.Ticket ticket : session.getTickets()) {
                                Map<String, Object> stats = new HashMap<>();
                                stats.put("price", ticket.getPrice());
                                stats.put("sold", soldByType.getOrDefault(ticket.getType(), 0));
                                stats.put("available", ticket.getAvailable());
                                stats.put("desc", ticket.getType());
                                typeStats.put(ticket.getType(), stats);
                            }
                        }

                        sessionData.put("tickets", ticketSummaries);
                        Map<String, Object> statsObj = new HashMap<>();
                        statsObj.put("typeStats", typeStats);
                        sessionData.put("stats", statsObj);

                        return sessionData;
                    })
                    .collect(Collectors.toList());

            // Calculate accumulated stats from actual bookings
            Map<String, Object> accumulatedStats = new HashMap<>();

            // Get actual tickets sold and revenue from bookings
            Long totalTicketsSold = bookingRepository.getTicketsSold(event);
            Double totalRevenue = bookingRepository.getTotalRevenueByEvent(event);

            // Get ticket type stats from bookings
            List<Map<String, Object>> bookingTicketStats = bookingRepository.getTicketTypeStatsByEvent(event);
            Map<String, Map<String, Object>> ticketTypeStats = new HashMap<>();

            for (Map<String, Object> stat : bookingTicketStats) {
                String type = (String) stat.get("type");
                Long sold = ((Number) stat.get("sold")).longValue();
                Double revenue = ((Number) stat.get("revenue")).doubleValue();

                Map<String, Object> typeStat = new HashMap<>();
                typeStat.put("sold", sold);
                typeStat.put("revenue", revenue);
                typeStat.put("price", sold > 0 ? revenue / sold : 0);
                ticketTypeStats.put(type, typeStat);
            }

            // Calculate available from session tickets
            int totalTicketsAvailable = 0;

            for (Session session : sessions) {
                if (session.getTickets() != null) {
                    for (Session.Ticket ticket : session.getTickets()) {
                        totalTicketsAvailable += ticket.getAvailable();
                    }
                }
            }

            accumulatedStats.put("totalTicketsSold", totalTicketsSold);
            accumulatedStats.put("totalRevenue", totalRevenue);
            accumulatedStats.put("totalTicketsAvailable", totalTicketsAvailable);
            accumulatedStats.put("averageTicketPrice",
                    totalTicketsSold > 0 ? Math.round(totalRevenue / totalTicketsSold) : 0);
            accumulatedStats.put("ticketTypeStats", ticketTypeStats);

            Map<String, Object> response = new HashMap<>();
            response.put("event", mapToSummary(event));
            response.put("sessions", sessionSummaries);
            response.put("accumulatedStats", accumulatedStats);

            return new ApiResponse<>(true, "Event data fetched successfully", response);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Error fetching event data: " + e.getMessage(), null);
        }
    }

    private EventSummaryResponse mapToSummary(Event event) {
        EventSummaryResponse response = new EventSummaryResponse();
        response.set_id(event.getId());
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setCategory(event.getCategory());
        response.setBannerImage(event.getBannerImage());
        response.setThumbnailImage(event.getThumbnailImage());
        response.setStatus(event.getStatus().name());
        response.setStartingPrice(event.getStartingPrice());
        response.setRecurrence(event.getRecurrence());
        response.setStartDate(event.getStartDate() != null ? event.getStartDate().toString() : null);
        response.setEndDate(event.getEndDate() != null ? event.getEndDate().toString() : null);
        response.setStartTime(formatTimeTo12Hour(event.getStartTime()));
        response.setEndTime(formatTimeTo12Hour(event.getEndTime()));
        response.setCreatedAt(event.getCreatedAt() != null ? event.getCreatedAt().toString() : null);

        LocationResponse location = new LocationResponse();
        location.setLabel(event.getLocationLabel());
        location.setCoordinates(List.of(event.getLocationLongitude(), event.getLocationLatitude()));
        response.setLocation(location);

        response.setAverageRating(event.getAverageRating());
        response.setInterestedUsers(event.getInterestedUsersList() != null ? event.getInterestedUsersList().size() : 0);

        return response;
    }

    private String formatTimeTo12Hour(String time24) {
        if (time24 == null || time24.isEmpty()) {
            return "";
        }
        try {
            String[] parts = time24.split(":");
            if (parts.length != 2) {
                return time24;
            }
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            String ampm = hour >= 12 ? "PM" : "AM";
            hour = hour % 12;
            if (hour == 0) {
                hour = 12;
            }

            return String.format("%d:%02d %s", hour, minute, ampm);
        } catch (Exception e) {
            return time24;
        }
    }
}
