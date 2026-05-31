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

        boolean existsByUserAndEvent(
                        User user,
                        Event event);

        // TODO: Update query to work with new Booking structure
        // The old query used b.ticketType.session.event and b.quantity
        // New structure has b.event and b.session directly
        // Need to calculate tickets sold from seats or ticketsSummary
        @Query("""
                        SELECT COUNT(b.id)
                        FROM Booking b
                        WHERE b.event = :event
                        """)
        Long getTicketsSold(Event event);
}