package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrganizerRepository
                extends JpaRepository<Organizer, Long> {

        Optional<Organizer> findByUser(User user);

        @Query("SELECT o FROM Organizer o WHERE o.user.email = :email")
        Optional<Organizer> findByUserEmail(@Param("email") String email);

        @Query("SELECT o FROM Organizer o WHERE o.user.id = :userId")
        Optional<Organizer> findByUserId(@Param("userId") Long userId);
}
