package com.eventhub.backend.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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

    @Column(length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    private String category;

    private String bannerImage;

    private String thumbnailImage;

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

    // Node.js matching fields
    private LocalDate startDate;

    private LocalDate endDate;

    private String startTime; // "HH:mm" format

    private String endTime; // "HH:mm" format

    private String recurrence; // "single", "multi-day", "weekly"

    @ElementCollection
    @CollectionTable(name = "event_selected_weekdays", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "weekday")
    private Set<String> selectedWeekdays = new HashSet<>();

    private String locationType; // "Point" for GeoJSON

    private Double locationLongitude; // coordinates[0]

    private Double locationLatitude; // coordinates[1]

    private String locationLabel;

    private Integer ageLimit;

    @ElementCollection
    @CollectionTable(name = "event_languages", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "language")
    private Set<String> languages = new HashSet<>();

    private Boolean isFeatured = false;

    private Double startingPrice;
}