package com.eventhub.backend.scheduler;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.repository.EventRepository;
import com.eventhub.backend.util.EventStatusUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    public EventStatusScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Scheduled(fixedRate = 60000) // Run every 60 seconds (1 minute)
    @Transactional
    public void updateEventStatuses() {
        System.out.println("=== Starting Event Status Update Scheduler ===");
        
        List<Event> allEvents = eventRepository.findAll();
        int updatedCount = 0;
        
        for (Event event : allEvents) {
            try {
                EventStatus currentStatus = event.getStatus();
                EventStatus calculatedStatus = EventStatusUtil.getCurrentStatus(event);
                
                // Skip DRAFT and CANCELLED events (manual overrides)
                if (currentStatus == EventStatus.DRAFT || currentStatus == EventStatus.CANCELLED) {
                    continue;
                }
                
                // Update if status has changed
                if (!calculatedStatus.equals(currentStatus)) {
                    System.out.println("Updating event " + event.getId() + " (" + event.getTitle() + 
                                     ") from " + currentStatus + " to " + calculatedStatus);
                    event.setStatus(calculatedStatus);
                    eventRepository.save(event);
                    updatedCount++;
                }
            } catch (Exception e) {
                System.err.println("Error updating status for event " + event.getId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("=== Event Status Update Scheduler Completed. Updated " + updatedCount + " events ===");
    }
}
