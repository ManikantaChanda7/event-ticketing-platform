package com.eventhub.backend.seed;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.Session;
import com.eventhub.backend.entity.Venue;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.OrganizerRepository;
import com.eventhub.backend.repository.SessionRepository;
import com.eventhub.backend.repository.VenueRepository;
import java.util.List;
import com.eventhub.backend.enums.EventStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.HashSet;
import java.util.Set;

@Service
public class EventGenerationService {
    private static final long MIN_ACTIVE_EVENTS = 150;

    private final EventRepository eventRepository;
    private final OrganizerRepository organizerRepository;
    private final VenueRepository venueRepository;
    private final SessionRepository sessionRepository;

    public EventGenerationService(
            EventRepository eventRepository,
            OrganizerRepository organizerRepository,
            VenueRepository venueRepository,
            SessionRepository sessionRepository) {

        this.eventRepository = eventRepository;
        this.organizerRepository = organizerRepository;
        this.venueRepository = venueRepository;
        this.sessionRepository = sessionRepository;
    }

    private String getRecurrence() {
        double r = Math.random();

        if (r < 0.75)
            return "single";
        if (r < 0.95)
            return "multi-day";

        return "weekly";
    }

    @Transactional
    public void generateDailyEvents() {
        long activeEvents = eventRepository.findAll().stream()
                .filter(event -> event.getStatus() == EventStatus.UPCOMING
                        || event.getStatus() == EventStatus.ONGOING)
                .count();

        System.out.println("Daily event generator started. Active events: " + activeEvents);

        if (activeEvents >= MIN_ACTIVE_EVENTS) {
            System.out.println("Active event target already satisfied. Skipping generation.");
            return;
        }

        List<Organizer> organizers = organizerRepository.findAll();
        List<Venue> venues = venueRepository.findAll();

        if (organizers.isEmpty() || venues.isEmpty()) {
            System.out.println("No organizers or venues found. Skipping generation.");
            return;
        }

        int eventsToGenerate = (int) Math.min(3, MIN_ACTIVE_EVENTS - activeEvents);

        System.out.println("Generating " + eventsToGenerate + " new upcoming events...");

        for (int i = 0; i < eventsToGenerate; i++) {
            Organizer organizer = organizers.get((int) (Math.random() * organizers.size()));
            Venue venue = venues.get((int) (Math.random() * venues.size()));

            String recurrence = getRecurrence();
            LocalDate startDate =
                    LocalDate.now().plusDays(7 + (int) (Math.random() * 53));

            Event event = new Event();

            event.setTitle("Live Event " + System.currentTimeMillis());
            event.setDescription("Auto generated upcoming event");
            event.setCategory("Music");

            event.setOrganizer(organizer);
            event.setVenue(venue);

            event.setStartDate(startDate);
            // event.setEndDate(startDate);
            if ("multi-day".equals(recurrence)) {
                event.setEndDate(startDate.plusDays(ThreadLocalRandom.current().nextInt(2, 6)));
            } else if ("weekly".equals(recurrence)) {
                event.setEndDate(startDate.plusDays(ThreadLocalRandom.current().nextInt(14, 31)));
            } else {
                event.setEndDate(startDate);
            }

            event.setStartTime("18:00");
            event.setEndTime("21:00");
            event.setRecurrence(recurrence);

            if ("weekly".equals(recurrence)) {
                Set<String> weekdays = new HashSet<>();
                weekdays.add(startDate.getDayOfWeek().name().substring(0, 1)
                        + startDate.getDayOfWeek().name().substring(1).toLowerCase());
                event.setSelectedWeekdays(weekdays);
            }

            event.setStatus(EventStatus.UPCOMING);

            event.setLocationLabel(venue.getName() + ", " + venue.getCity());
            event.setLocationLatitude(venue.getLatitude());
            event.setLocationLongitude(venue.getLongitude());

            event.setStartingPrice(499.0);
            event.setInterestedUsers(0);

            Event savedEvent = eventRepository.save(event);

            if ("weekly".equals(recurrence)) {

                for (LocalDate sessionDate = startDate;
                        !sessionDate.isAfter(savedEvent.getEndDate());
                        sessionDate = sessionDate.plusWeeks(1)) {

                    Session session = buildSession(savedEvent, venue, sessionDate);
                    sessionRepository.save(session);
                }

            } else if ("multi-day".equals(recurrence)) {

                for (LocalDate sessionDate = startDate;
                        !sessionDate.isAfter(savedEvent.getEndDate());
                        sessionDate = sessionDate.plusDays(1)) {

                    Session session = buildSession(savedEvent, venue, sessionDate);
                    sessionRepository.save(session);
                }

            } else {

                Session session = buildSession(savedEvent, venue, startDate);
                sessionRepository.save(session);
            }

            System.out.println("Created event: " + savedEvent.getTitle());
        }

        System.out.println("Daily event generator completed. Generated " + eventsToGenerate + " event(s) with session(s).");
    }

    private Session buildSession(Event event, Venue venue, LocalDate sessionDate) {
        Session session = new Session();
        session.setEvent(event);
        session.setDate(sessionDate);
        session.setStartTime("18:00");
        session.setEndTime("21:00");
        session.setOccupancy(0);
        session.setReleaseDate(sessionDate.minusDays(14).atStartOfDay());

        Session.Ticket ticket = new Session.Ticket();
        ticket.setType("General");
        ticket.setPrice(499.0);
        ticket.setAvailable(venue.getCapacity());
        ticket.setTotalSeats(venue.getCapacity());

        java.util.List<Session.Ticket> tickets = new java.util.ArrayList<>();
        tickets.add(ticket);
        session.setTickets(tickets);

        return session;
    }

}