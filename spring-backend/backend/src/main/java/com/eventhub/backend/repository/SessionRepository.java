package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Organizer;
import com.eventhub.backend.entity.Session;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SessionRepository
                extends JpaRepository<Session, Long> {

        @EntityGraph(attributePaths = {
                        "tickets"
        })
        List<Session> findByEvent(Event event);

        long countByEvent(Event event);

        boolean existsByEventAndDateAndStartTime(
                        Event event,
                        LocalDate date,
                        String startTime);

        @Query("SELECT s FROM Session s JOIN s.event e WHERE e.organizer = :organizer")
        List<Session> findByEventOrganizer(@Param("organizer") Organizer organizer);
}