package com.eventhub.backend.dto;

import com.eventhub.backend.entity.Event;

public class EventStatsDTO {
    private Event event;
    private Long ticketsSold;

    public EventStatsDTO() {}

    public EventStatsDTO(Event event, Long ticketsSold) {
        this.event = event;
        this.ticketsSold = ticketsSold;
    }

    public EventStatsDTO(Event event, Number ticketsSold) {
        this.event = event;
        this.ticketsSold = ticketsSold != null ? ticketsSold.longValue() : 0L;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Long getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(Long ticketsSold) {
        this.ticketsSold = ticketsSold;
    }
}
