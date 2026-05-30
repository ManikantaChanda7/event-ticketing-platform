package com.eventhub.backend.entity;

import java.util.HashSet;
import java.util.Set;

import com.eventhub.backend.enums.EventStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;

    private String bannerImage;

    @Enumerated(EnumType.STRING)
    private EventStatus status = EventStatus.DRAFT;

    @ManyToOne
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @ManyToOne
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false)
    private Double averageRating = 0.0;

    @Column(nullable = false)
    private Integer interestedUsers = 0;

    @ManyToMany(mappedBy = "interestedEvents")
    private Set<User> interestedUsersList = new HashSet<>();
}