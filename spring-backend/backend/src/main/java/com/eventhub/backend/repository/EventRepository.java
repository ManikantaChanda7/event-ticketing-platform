package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eventhub.backend.entity.Organizer;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(Organizer organizer);
}