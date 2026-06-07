package com.eventhub.backend.util;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.repository.EventRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventStatusUtil {

    public static EventStatus getCurrentStatus(Event event) {
        System.out.println("=== Dynamic Status Calculation ===");
        System.out.println("Event ID: " + event.getId());
        System.out.println("Event Title: " + event.getTitle());
        System.out.println("Stored Status: " + event.getStatus());
        System.out.println("Start Date: " + event.getStartDate());
        System.out.println("End Date: " + event.getEndDate());
        System.out.println("Start Time: " + event.getStartTime());
        System.out.println("End Time: " + event.getEndTime());

        // If status is manually set to DRAFT or CANCELLED, keep it
        if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.CANCELLED) {
            System.out.println("Calculated Status: " + event.getStatus() + " (manual override)");
            return event.getStatus();
        }

        // Calculate dynamic status based on current time
        LocalDate now = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        System.out.println("Current Date: " + now);
        System.out.println("Current Time: " + nowTime);

        LocalDate startDate = event.getStartDate();
        LocalDate endDate = event.getEndDate();
        String startTimeStr = event.getStartTime();
        String endTimeStr = event.getEndTime();

        // If dates are not set, default to UPCOMING
        if (startDate == null) {
            System.out.println("Calculated Status: UPCOMING (no start date)");
            return EventStatus.UPCOMING;
        }

        // Parse start and end times
        LocalTime startTime = parseTime(startTimeStr);
        LocalTime endTime = parseTime(endTimeStr);
        System.out.println("Parsed Start Time: " + startTime);
        System.out.println("Parsed End Time: " + endTime);

        // Event hasn't started yet
        if (now.isBefore(startDate)) {
            System.out.println("Calculated Status: UPCOMING (before start date)");
            return EventStatus.UPCOMING;
        }

        // Event is on the current day
        if (now.isEqual(startDate)) {
            if (startTime != null && nowTime.isBefore(startTime)) {
                System.out.println("Calculated Status: UPCOMING (before start time on start day)");
                return EventStatus.UPCOMING;
            }
            if (endTime != null && nowTime.isAfter(endTime)) {
                System.out.println("Calculated Status: COMPLETED (after end time on start day)");
                return EventStatus.COMPLETED;
            }
            System.out.println("Calculated Status: ONGOING (on start day within time range)");
            return EventStatus.ONGOING;
        }

        // Event is after start date
        if (endDate != null) {
            if (now.isBefore(endDate)) {
                System.out.println("Calculated Status: ONGOING (between start and end date)");
                return EventStatus.ONGOING;
            }
            if (now.isEqual(endDate)) {
                if (endTime != null && nowTime.isAfter(endTime)) {
                    System.out.println("Calculated Status: COMPLETED (after end time on end day)");
                    return EventStatus.COMPLETED;
                }
                System.out.println("Calculated Status: ONGOING (on end day within time range)");
                return EventStatus.ONGOING;
            }
        }

        // Event has ended
        System.out.println("Calculated Status: COMPLETED (after end date)");
        return EventStatus.COMPLETED;
    }

    public static EventStatus getCurrentStatusAndUpdate(Event event, EventRepository eventRepository) {
        EventStatus calculatedStatus = getCurrentStatus(event);

        // If status has changed, update database
        if (!calculatedStatus.equals(event.getStatus())) {
            System.out.println(
                    "Status changed from " + event.getStatus() + " to " + calculatedStatus + " - updating database");
            event.setStatus(calculatedStatus);
            eventRepository.save(event);
        } else {
            System.out.println("Status unchanged: " + calculatedStatus);
        }

        return calculatedStatus;
    }

    private static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        try {
            String[] parts = timeStr.split(":");
            if (parts.length >= 2) {
                return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        } catch (Exception e) {
            // If parsing fails, return null
        }
        return null;
    }
}
