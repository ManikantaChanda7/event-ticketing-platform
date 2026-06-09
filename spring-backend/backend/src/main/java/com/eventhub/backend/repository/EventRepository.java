package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eventhub.backend.entity.Organizer;

public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findByOrganizer(Organizer organizer);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findByCategory(String category);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findByTitleContainingIgnoreCase(String keyword);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findTop5ByCategoryAndIdNot(
            String category,
            Long eventId);

    @Query("""
            SELECT DISTINCT e.category
            FROM Event e
            """)
    List<String> findDistinctCategories();

    @Query("""
            SELECT DISTINCT e
            FROM Event e
            WHERE e.status IN (
                com.eventhub.backend.enums.EventStatus.UPCOMING,
                com.eventhub.backend.enums.EventStatus.ONGOING
            )
            AND e.id NOT IN :excludedIds
            AND (
                e.category IN :categories
                OR e.organizer.id IN :organizerIds
            )
            ORDER BY e.interestedUsers DESC
            """)
    List<Event> findRecommendedEvents(
            @Param("categories") List<String> categories,
            @Param("organizerIds") List<Long> organizerIds,
            @Param("excludedIds") List<Long> excludedIds);

    @Query("""
            SELECT e
            FROM Event e
            WHERE e.id <> :eventId
            AND e.status IN (
                com.eventhub.backend.enums.EventStatus.UPCOMING,
                com.eventhub.backend.enums.EventStatus.ONGOING
            )
            AND (
                e.category = :category
                OR e.organizer.id = :organizerId
            )
            ORDER BY e.interestedUsers DESC
            """)
    List<Event> findSimilarEvents(
            @Param("eventId") Long eventId,
            @Param("category") String category,
            @Param("organizerId") Long organizerId);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findByOrganizerId(Long organizerId);

    long countByOrganizer(Organizer organizer);

    long countByOrganizerAndStatusIn(Organizer organizer, List<String> statuses);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findByOrganizerAndStatusOrderByStartDateAsc(Organizer organizer, String status);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    List<Event> findByOrganizerOrderByCreatedAtDesc(Organizer organizer);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    org.springframework.data.domain.Page<Event> findByOrganizerOrderByCreatedAtDesc(Organizer organizer, org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    @Query("SELECT e FROM Event e WHERE e.status != :status")
    List<Event> findAllExcludingStatus(@Param("status") com.eventhub.backend.enums.EventStatus status);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    @Query("SELECT e FROM Event e WHERE e.category = :category AND e.status != :status")
    List<Event> findByCategoryExcludingStatus(@Param("category") String category,
                                               @Param("status") com.eventhub.backend.enums.EventStatus status);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) AND e.status != :status")
    List<Event> findByTitleContainingIgnoreCaseExcludingStatus(@Param("keyword") String keyword,
                                                                @Param("status") com.eventhub.backend.enums.EventStatus status);

    @EntityGraph(attributePaths = {"venue", "organizer"})
    @Query("SELECT e FROM Event e WHERE e.organizer.id = :organizerId AND e.status != :status")
    org.springframework.data.domain.Page<Event> findByOrganizerIdAndStatusNot(
            @Param("organizerId") Long organizerId,
            @Param("status") com.eventhub.backend.enums.EventStatus status,
            org.springframework.data.domain.Pageable pageable);

    @Query(nativeQuery = true, value = """
            SELECT DISTINCT e.* FROM events e
            JOIN venues v ON v.id = e.venue_id
            WHERE e.status != 'COMPLETED'
            AND (CAST(:categories AS VARCHAR) IS NULL OR e.category = ANY(string_to_array(CAST(:categories AS VARCHAR), ',')))
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
            AND (:userLat IS NULL OR :userLon IS NULL OR
                 (6371 * acos(cos(radians(:userLat)) * cos(radians(v.latitude)) *
                 cos(radians(v.longitude) - radians(:userLon)) +
                 sin(radians(:userLat)) * sin(radians(v.latitude)))) <= 10)
            LIMIT :limit OFFSET :offset
            """)
    List<Event> filterEventsPaginated(
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
            @Param("endTime") String endTime,
            @Param("userLat") Double userLat,
            @Param("userLon") Double userLon,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);

    @Query(nativeQuery = true, value = """
            SELECT COUNT(DISTINCT e.id) FROM events e
            JOIN venues v ON v.id = e.venue_id
            WHERE e.status != 'COMPLETED'
            AND (CAST(:categories AS VARCHAR) IS NULL OR e.category = ANY(string_to_array(CAST(:categories AS VARCHAR), ',')))
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
            AND (:userLat IS NULL OR :userLon IS NULL OR
                 (6371 * acos(cos(radians(:userLat)) * cos(radians(v.latitude)) *
                 cos(radians(v.longitude) - radians(:userLon)) +
                 sin(radians(:userLat)) * sin(radians(v.latitude)))) <= 10)
            """)
    Long countFilterEvents(
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
            @Param("endTime") String endTime,
            @Param("userLat") Double userLat,
            @Param("userLon") Double userLon);

    @Query("""
            SELECT new com.eventhub.backend.dto.EventStatsDTO(
                e,
                COALESCE(COUNT(bs), 0)
            )
            FROM Event e
            LEFT JOIN Booking b ON b.event = e
            LEFT JOIN b.seats bs
            WHERE e.status != :completedStatus
            GROUP BY e
            ORDER BY (COALESCE(e.averageRating, 0.0) * 10 + COALESCE(e.interestedUsers, 0) + COALESCE(COUNT(bs), 0)) DESC
            """)
    List<com.eventhub.backend.dto.EventStatsDTO> findTrendingEventsWithStats(@Param("completedStatus") com.eventhub.backend.enums.EventStatus completedStatus);

    @Query(nativeQuery = true, value = """
            SELECT e.* FROM events e
            JOIN venues v ON v.id = e.venue_id
            WHERE e.status != 'COMPLETED'
            AND (:userLat IS NULL OR :userLon IS NULL OR
                 (6371 * acos(cos(radians(:userLat)) * cos(radians(v.latitude)) *
                 cos(radians(v.longitude) - radians(:userLon)) +
                 sin(radians(:userLat)) * sin(radians(v.latitude)))) <= 20)
            ORDER BY e.interested_users DESC NULLS LAST, e.average_rating DESC NULLS LAST
            LIMIT :limit
            """)
    List<Event> findPopularEvents(
            @Param("userLat") Double userLat,
            @Param("userLon") Double userLon,
            @Param("limit") int limit);
}