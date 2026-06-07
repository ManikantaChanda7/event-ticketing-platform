package com.eventhub.backend.util;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.enums.EventStatus;
import com.eventhub.backend.repository.EventRepository;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventStatusUtil {

    public static EventStatus getCurrentStatus(Event event) {

        // If status is manually set to DRAFT or CANCELLED, keep it
        if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.CANCELLED) {
            return event.getStatus();
        }

        // Calculate dynamic status based on current time
        LocalDate now = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        LocalDate startDate = event.getStartDate();
        LocalDate endDate = event.getEndDate();
        String startTimeStr = event.getStartTime();
        String endTimeStr = event.getEndTime();

        // If dates are not set, default to UPCOMING
        if (startDate == null) {
            return EventStatus.UPCOMING;
        }

        // Parse start and end times
        LocalTime startTime = parseTime(startTimeStr);
        LocalTime endTime = parseTime(endTimeStr);

        // Event hasn't started yet
        if (now.isBefore(startDate)) {
            return EventStatus.UPCOMING;
        }

        // Event is on the current day
        if (now.isEqual(startDate)) {
            if (startTime != null && nowTime.isBefore(startTime)) {
                return EventStatus.UPCOMING;
            }
            if (endTime != null && nowTime.isAfter(endTime)) {
                return EventStatus.COMPLETED;
            }
            return EventStatus.ONGOING;
        }

        // Event is after start date
        if (endDate != null) {
            if (now.isBefore(endDate)) {
                return EventStatus.ONGOING;
            }
            if (now.isEqual(endDate)) {
                if (endTime != null && nowTime.isAfter(endTime)) {
                    return EventStatus.COMPLETED;
                }
                return EventStatus.ONGOING;
            }
        }

        // Event has ended
        return EventStatus.COMPLETED;
    }

    public static EventStatus getCurrentStatusAndUpdate(Event event, EventRepository eventRepository) {
        EventStatus calculatedStatus = getCurrentStatus(event);

        // If status has changed, update database
        if (!calculatedStatus.equals(event.getStatus())) {
            event.setStatus(calculatedStatus);
            eventRepository.save(event);
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
