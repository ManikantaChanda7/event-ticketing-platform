package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT v.* FROM venues v
            WHERE v.id NOT IN (
                SELECT DISTINCT e.venue_id FROM events e
                WHERE e.venue_id IS NOT NULL
                AND NOT (e.end_date < :startDate OR e.start_date > :endDate)
            )
            AND (:searchTerm IS NULL OR :searchTerm = '' OR 
                 LOWER(v.name || ' ' || COALESCE(v.city, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            """)
    List<Venue> findAvailableVenues(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("searchTerm") String searchTerm);
}