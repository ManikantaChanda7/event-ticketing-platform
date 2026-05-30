package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.eventhub.backend.entity.Organizer;

public interface EventRepository extends JpaRepository<Event, Long> {

        List<Event> findByOrganizer(Organizer organizer);

        List<Event> findByCategory(String category);

        List<Event> findByTitleContainingIgnoreCase(String keyword);

        List<Event> findTop5ByCategoryAndIdNot(
                        String category,
                        Long eventId);

        @Query("""
                        SELECT DISTINCT e
                        FROM Event e
                        WHERE e.category IN :categories
                        AND e.id NOT IN :excludedIds
                        """)
        List<Event> findRecommendedEvents(
                        List<String> categories,
                        List<Long> excludedIds);

        List<Event> findByOrganizerId(Long organizerId);

        @Query("""
                        SELECT e
                        FROM Event e
                        WHERE
                        (:category IS NULL OR e.category = :category)
                        AND
                        (:city IS NULL OR
                        LOWER(e.venue.city) LIKE
                        LOWER(CONCAT('%', :city, '%')))
                        AND
                        (:keyword IS NULL OR
                        LOWER(e.title) LIKE
                        LOWER(CONCAT('%', :keyword, '%')))
                        """)
        List<Event> filterEvents(
                        String category,
                        String city,
                        String keyword);
}