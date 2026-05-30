package com.eventhub.backend.dto;

public class EventScore {

    private EventSummaryResponse event;
    private Double score;

    public EventSummaryResponse getEvent() {
        return event;
    }

    public void setEvent(EventSummaryResponse event) {
        this.event = event;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}