package com.eventhub.backend.entity;

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
}