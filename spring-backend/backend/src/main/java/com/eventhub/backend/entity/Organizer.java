package com.eventhub.backend.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "organizers")
@Getter
@Setter
public class Organizer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String phone;

    private String organizerProfileImage;

    private String orgName; // Changed from organizationName to match Node.js

    private String orgEmail; // Added to match Node.js

    private String orgDescription; // Changed from description to match Node.js

    private String organizerBannerImage; // Added to match Node.js

    @ElementCollection
    @CollectionTable(name = "organizer_specialities", joinColumns = @JoinColumn(name = "organizer_id"))
    @Column(name = "speciality")
    private Set<String> orgSpecialities = new HashSet<>();

    private Double averageRating = 0.0; // Added to match Node.js

    private Integer totalReviews = 0; // Added to match Node.js

    @ManyToMany
    @JoinTable(name = "organizer_events_hosted", joinColumns = @JoinColumn(name = "organizer_id"), inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> eventsHosted = new HashSet<>();
}