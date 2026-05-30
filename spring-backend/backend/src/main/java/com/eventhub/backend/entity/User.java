package com.eventhub.backend.entity;

import java.util.HashSet;
import java.util.Set;

import com.eventhub.backend.enums.Role;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean isOAuth = false;

    private String phone;

    private String userProfileImage;

    private Double latitude;

    private Double longitude;

    private String locationLabel;

    @ManyToMany
    @JoinTable(name = "user_interested_events", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> interestedEvents = new HashSet<>();
}