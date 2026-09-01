package com.eventhub.backend.seed;

import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.repository.SessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Component
public class DailyEventGeneratorScheduler {

    private final EventGenerationService eventGenerationService;
    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;

    public DailyEventGeneratorScheduler(
            EventGenerationService eventGenerationService,
            EventRepository eventRepository,
            SessionRepository sessionRepository) {
        this.eventGenerationService = eventGenerationService;
        this.eventRepository = eventRepository;
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void generateEventsDaily() {
        eventGenerationService.generateDailyEvents();
    }

    // Manual trigger endpoint for testing
    @PostMapping("/generate-events")
    public String generateEventsNow() {
        eventGenerationService.generateDailyEvents();
        return "Event generation triggered manually";
    }

    // Delete upcoming and ongoing events endpoint
    @PostMapping("/delete-events")
    @Transactional
    public String deleteUpcomingAndOngoingEvents() {
        List<Event> eventsToDelete = eventRepository.findAll().stream()
                .filter(event -> event.getStatus() == EventStatus.UPCOMING
                        || event.getStatus() == EventStatus.ONGOING)
                .toList();

        long eventCount = eventsToDelete.size();
        long sessionCount = 0;

        for (Event event : eventsToDelete) {
            // Delete associated sessions first, then count them
            long eventSessionCount = sessionRepository.countByEvent(event);
            sessionRepository.deleteAll(sessionRepository.findByEvent(event));
            sessionCount += eventSessionCount;
        }

        eventRepository.deleteAll(eventsToDelete);

        return "Deleted " + eventCount + " upcoming and ongoing events and " + sessionCount + " sessions";
    }
}
