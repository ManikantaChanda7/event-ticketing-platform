package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

        @Query(nativeQuery = true, value = """
                        SELECT DISTINCT e.* FROM events e
                        JOIN venues v ON v.id = e.venue_id
                        WHERE (CAST(:categories AS VARCHAR) IS NULL OR e.category = ANY(string_to_array(CAST(:categories AS VARCHAR), ',')))
                        AND (CAST(:city AS VARCHAR) IS NULL OR LOWER(v.city) = LOWER(CAST(:city AS VARCHAR)))
                        AND (CAST(:keywordPattern AS VARCHAR) IS NULL OR LOWER(e.title) LIKE CAST(:keywordPattern AS VARCHAR) OR LOWER(e.description) LIKE CAST(:keywordPattern AS VARCHAR))
                        AND (CAST(:isFeatured AS BOOLEAN) IS NULL OR e.is_featured = CAST(:isFeatured AS BOOLEAN))
                        AND (CAST(:recurrence AS VARCHAR) IS NULL OR e.recurrence = CAST(:recurrence AS VARCHAR))
                        AND (CAST(:minRating AS DOUBLE PRECISION) IS NULL OR e.average_rating >= CAST(:minRating AS DOUBLE PRECISION))
                        AND (CAST(:startDate AS DATE) IS NULL OR e.start_date >= CAST(:startDate AS DATE))
                        AND (CAST(:endDate AS DATE) IS NULL OR e.end_date <= CAST(:endDate AS DATE))
                        AND (CAST(:languages AS VARCHAR) IS NULL OR EXISTS(SELECT 1 FROM event_languages el WHERE el.event_id = e.id AND el.language = ANY(string_to_array(CAST(:languages AS VARCHAR), ','))))
                        AND (CAST(:ageLimit AS INTEGER) IS NULL OR e.age_limit <= CAST(:ageLimit AS INTEGER))
                        AND (CAST(:startTime AS VARCHAR) IS NULL OR e.start_time >= CAST(:startTime AS VARCHAR))
                        AND (CAST(:endTime AS VARCHAR) IS NULL OR e.end_time <= CAST(:endTime AS VARCHAR))
                        """)
        List<Event> filterEvents(
                        @Param("categories") String categories,
                        @Param("city") String city,
                        @Param("keywordPattern") String keywordPattern,
                        @Param("isFeatured") Boolean isFeatured,
                        @Param("recurrence") String recurrence,
                        @Param("minRating") Double minRating,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("languages") String languages,
                        @Param("ageLimit") Integer ageLimit,
                        @Param("startTime") String startTime,
                        @Param("endTime") String endTime);
}