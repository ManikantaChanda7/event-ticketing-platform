package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Session;
import com.eventhub.backend.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository
        extends JpaRepository<TicketType, Long> {

    List<TicketType> findBySession(Session session);
}