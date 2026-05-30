package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository
        extends JpaRepository<Session, Long> {

    List<Session> findByEvent(Event event);
}