package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizerRepository
                extends JpaRepository<Organizer, Long> {

        Optional<Organizer> findByUser(User user);
}
