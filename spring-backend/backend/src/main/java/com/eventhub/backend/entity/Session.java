package com.eventhub.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "sessions")
@Getter
@Setter
public class Session extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate sessionDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;

    private Integer availableSeats;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
}