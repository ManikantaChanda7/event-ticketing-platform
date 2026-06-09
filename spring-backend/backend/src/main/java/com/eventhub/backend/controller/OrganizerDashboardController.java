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
import com.eventhub.backend.util.EventStatusUtil;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @GetMapping("/profile")
    public ApiResponse<com.eventhub.backend.dto.OrganizerProfileResponse> getOrganizerProfile(Authentication authentication) {
        Organizer organizer = getOrganizerFromUser(authentication);
        com.eventhub.backend.dto.OrganizerProfileResponse response = mapToOrganizerProfileResponse(organizer);
        return new ApiResponse<>(true, "Organizer profile fetched successfully", response);
    }

    @GetMapping("/profile/user/{userId}")
    public ApiResponse<com.eventhub.backend.dto.OrganizerProfileResponse> getOrganizerProfileByUserId(@PathVariable Long userId) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
        com.eventhub.backend.dto.OrganizerProfileResponse response = mapToOrganizerProfileResponse(organizer);
        return new ApiResponse<>(true, "Organizer profile fetched successfully", response);
    }

    private com.eventhub.backend.dto.OrganizerProfileResponse mapToOrganizerProfileResponse(Organizer organizer) {
        com.eventhub.backend.dto.OrganizerProfileResponse response = new com.eventhub.backend.dto.OrganizerProfileResponse();
        response.setId(organizer.getId());
        response.setOrgName(organizer.getOrgName());
        response.setOrgEmail(organizer.getOrgEmail());
        response.setOrgDescription(organizer.getOrgDescription());
        response.setPhone(organizer.getPhone());
        response.setOrganizerProfileImage(organizer.getOrganizerProfileImage());
        response.setOrganizerBannerImage(organizer.getOrganizerBannerImage());
        response.setOrgSpecialities(organizer.getOrgSpecialities());
        response.setAverageRating(organizer.getAverageRating());
        response.setTotalReviews(organizer.getTotalReviews());
        return response;
    }

    @PutMapping("/profile")
    public ApiResponse<com.eventhub.backend.dto.OrganizerProfileResponse> updateOrganizerProfile(
            Authentication authentication,
            @RequestBody Map<String, Object> updates) {
        Organizer organizer = getOrganizerFromUser(authentication);

        if (updates.containsKey("orgName")) {
            organizer.setOrgName((String) updates.get("orgName"));
        }
        if (updates.containsKey("orgEmail")) {
            organizer.setOrgEmail((String) updates.get("orgEmail"));
        }
        if (updates.containsKey("orgDescription")) {
            organizer.setOrgDescription((String) updates.get("orgDescription"));
        }
        if (updates.containsKey("phone")) {
            organizer.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("organizerProfileImage")) {
            organizer.setOrganizerProfileImage((String) updates.get("organizerProfileImage"));
        }
        if (updates.containsKey("organizerBannerImage")) {
            organizer.setOrganizerBannerImage((String) updates.get("organizerBannerImage"));
        }
        if (updates.containsKey("orgSpecialities")) {
            organizer.setOrgSpecialities(new HashSet<>((Set<String>) updates.get("orgSpecialities")));
        }

        organizerRepository.save(organizer);
        com.eventhub.backend.dto.OrganizerProfileResponse response = mapToOrganizerProfileResponse(organizer);
        return new ApiResponse<>(true, "Organizer profile updated successfully", response);
    }

    @PutMapping("/profile/user/{userId}")
    public ApiResponse<com.eventhub.backend.dto.OrganizerProfileResponse> updateOrganizerProfileByUserId(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> updates) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (updates.containsKey("orgName")) {
            organizer.setOrgName((String) updates.get("orgName"));
        }
        if (updates.containsKey("orgEmail")) {
            organizer.setOrgEmail((String) updates.get("orgEmail"));
        }
        if (updates.containsKey("orgDescription")) {
            organizer.setOrgDescription((String) updates.get("orgDescription"));
        }
        if (updates.containsKey("phone")) {
            organizer.setPhone((String) updates.get("phone"));
        }
        if (updates.containsKey("organizerProfileImage")) {
            organizer.setOrganizerProfileImage((String) updates.get("organizerProfileImage"));
        }
        if (updates.containsKey("organizerBannerImage")) {
            organizer.setOrganizerBannerImage((String) updates.get("organizerBannerImage"));
        }
        if (updates.containsKey("orgSpecialities")) {
            organizer.setOrgSpecialities(new HashSet<>((Set<String>) updates.get("orgSpecialities")));
        }

        organizerRepository.save(organizer);
        com.eventhub.backend.dto.OrganizerProfileResponse response = mapToOrganizerProfileResponse(organizer);
        return new ApiResponse<>(true, "Organizer profile updated successfully", response);
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

        long totalTicketsSold = bookingRepository.getTotalTicketsSoldByOrganizer(organizer);
        Double totalRevenue = bookingRepository.getTotalRevenueByOrganizer(organizer);
        if (totalRevenue == null) totalRevenue = 0.0;

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

        List<Map<String, Object>> revenueByCategory = bookingRepository.getRevenueByCategory(organizer).stream()
                .map(dto -> {
                    Map<String, Object> rev = new HashMap<>();
                    rev.put("_id", dto.getCategory());
                    rev.put("revenue", dto.getRevenue());
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
            List<com.eventhub.backend.dto.TopSellingEventDTO> topSelling = bookingRepository.getTopSellingEvents(organizer);

            List<Map<String, Object>> topSellingMaps = topSelling.stream()
                    .map(dto -> {
                        Map<String, Object> event = new HashMap<>();
                        event.put("eventId", dto.getEventId());
                        event.put("title", dto.getTitle());
                        event.put("ticketsSold", dto.getTicketsSold());
                        event.put("revenue", dto.getRevenue());
                        return event;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> topSellingData = new HashMap<>();
            topSellingData.put("topSelling", topSellingMaps);

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

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page - 1, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        org.springframework.data.domain.Page<Event> eventPage = eventRepository.findByOrganizerOrderByCreatedAtDesc(organizer, pageable);

        List<EventSummaryResponse> paginatedEvents = eventPage.getContent()
                .stream()
                .map(this::mapToSummary)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("data", paginatedEvents);

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("total", eventPage.getTotalElements());
        pagination.put("page", page);
        pagination.put("limit", limit);
        pagination.put("totalPages", eventPage.getTotalPages());
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

            // Fetch all session ticket stats in a single query to avoid N+1
            List<com.eventhub.backend.dto.SessionTicketStatsDTO> allSessionStats = bookingRepository.getAllSessionTicketStatsByEvent(event);
            Map<Long, Map<String, Map<String, Object>>> sessionStatsMap = new HashMap<>();
            for (com.eventhub.backend.dto.SessionTicketStatsDTO stat : allSessionStats) {
                Session session = stat.getSession();
                String type = stat.getType();
                Long sold = stat.getSold();
                Double revenue = stat.getRevenue();

                sessionStatsMap
                    .computeIfAbsent(session.getId(), k -> new HashMap<>())
                    .put(type, Map.of(
                        "sold", sold,
                        "revenue", revenue
                    ));
            }

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
                            // Use pre-fetched stats instead of N+1 query
                            Map<String, Map<String, Object>> sessionStatData = sessionStatsMap.getOrDefault(session.getId(), new HashMap<>());

                            ticketSummaries = session.getTickets().stream()
                                    .map(ticket -> {
                                        Map<String, Object> ticketData = new HashMap<>();
                                        ticketData.put("type", ticket.getType());
                                        ticketData.put("price", ticket.getPrice());
                                        
                                        // Calculate actual available seats by subtracting sold from total
                                        Map<String, Object> typeStat = sessionStatData.getOrDefault(ticket.getType(), Map.of("sold", 0, "revenue", 0.0));
                                        Object soldObj = typeStat.get("sold");
                                        Long sold = soldObj instanceof Number ? ((Number) soldObj).longValue() : 0L;
                                        Long totalSeats = ticket.getTotalSeats() != null ? ticket.getTotalSeats().longValue() : 0L;
                                        Long available = totalSeats - sold;
                                        
                                        ticketData.put("available", available);
                                        ticketData.put("totalSeats", totalSeats);
                                        ticketData.put("sold", sold);
                                        return ticketData;
                                    })
                                    .collect(Collectors.toList());

                            // Build type stats for the session
                            for (Session.Ticket ticket : session.getTickets()) {
                                Map<String, Object> stats = new HashMap<>();
                                stats.put("price", ticket.getPrice());
                                Map<String, Object> typeStat = sessionStatData.getOrDefault(ticket.getType(), Map.of("sold", 0, "revenue", 0.0));
                                Object soldObj = typeStat.get("sold");
                                Long sold = soldObj instanceof Number ? ((Number) soldObj).longValue() : 0L;
                                Long totalSeats = ticket.getTotalSeats() != null ? ticket.getTotalSeats().longValue() : 0L;
                                stats.put("sold", sold);
                                stats.put("available", totalSeats - sold);
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
            List<com.eventhub.backend.dto.TicketTypeStatsDTO> bookingTicketStats = bookingRepository.getTicketTypeStatsByEvent(event);
            Map<String, Map<String, Object>> ticketTypeStats = new HashMap<>();

            for (com.eventhub.backend.dto.TicketTypeStatsDTO stat : bookingTicketStats) {
                String type = stat.getType();
                Long sold = stat.getSold();
                Double revenue = stat.getRevenue();

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
        response.setStatus(EventStatusUtil.getCurrentStatus(event).name());
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
