package com.eventhub.backend.seed;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyEventGeneratorScheduler {

    private final EventGenerationService eventGenerationService;

    public DailyEventGeneratorScheduler(EventGenerationService eventGenerationService) {
        this.eventGenerationService = eventGenerationService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void generateEventsDaily() {
        eventGenerationService.generateDailyEvents();
    }
}
