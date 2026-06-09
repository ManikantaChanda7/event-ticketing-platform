package com.eventhub.backend.repository;

import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.interestedEvents WHERE u.email = :email")
    Optional<User> findByEmailWithInterests(@Param("email") String email);

    @Query("""
            SELECT e FROM User u
            JOIN u.interestedEvents e
            WHERE u.email = :email
            AND (:status IS NULL OR e.status = :status)
            """)
    Page<Event> findInterestedEventsByEmail(@Param("email") String email,
                                           @Param("status") EventStatus status,
                                           Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.interestedEvents WHERE u.email = :email")
    Optional<User> findUserWithInterestsForLogin(@Param("email") String email);
}