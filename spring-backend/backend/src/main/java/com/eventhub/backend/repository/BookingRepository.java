package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);

    boolean existsByUserAndTicketType_Session_Event(
            User user,
            Event event);

    @Query("""
            SELECT COALESCE(SUM(b.quantity),0)
            FROM Booking b
            WHERE b.ticketType.session.event = :event
            """)
    Long getTicketsSold(Event event);
}