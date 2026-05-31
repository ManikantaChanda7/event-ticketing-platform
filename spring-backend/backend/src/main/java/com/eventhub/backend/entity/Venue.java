package com.eventhub.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "venues")
@Getter
@Setter
public class Venue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String address;

    private String city;

    private String state;

    private String country;

    private Integer capacity;

    private Double latitude;

    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String seatingLayout; // Added to match Node.js - stores seating layout as JSON
}