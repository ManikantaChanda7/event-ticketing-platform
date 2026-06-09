package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Review;
import com.eventhub.backend.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Review> findByEvent(Event event);

    Optional<Review> findByUserAndEvent(
            User user,
            Event event);

    @Query("""
            SELECT AVG(r.rating)
            FROM Review r
            WHERE r.event = :event
            """)
    Double findAverageRatingByEvent(Event event);

    Long countByEvent(Event event);
}