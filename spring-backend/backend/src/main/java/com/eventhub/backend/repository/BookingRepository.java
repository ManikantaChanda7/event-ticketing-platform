package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Session;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface BookingRepository
                extends JpaRepository<Booking, Long> {

        List<Booking> findByUser(User user);

        boolean existsByUserAndEvent(
                        User user,
                        Event event);

        @Query("""
                        SELECT COALESCE(COUNT(bs), 0)
                        FROM Booking b
                        JOIN b.seats bs
                        WHERE b.event = :event
                        """)
        Long getTicketsSold(@Param("event") Event event);

        @Query("""
                        SELECT COALESCE(COUNT(bs), 0)
                        FROM Booking b
                        JOIN b.seats bs
                        WHERE b.event.organizer = :organizer
                        """)
        Long getTotalTicketsSoldByOrganizer(@Param("organizer") Organizer organizer);

        @Query("""
                        SELECT COALESCE(SUM(b.totalAmount), 0)
                        FROM Booking b
                        WHERE b.event.organizer = :organizer
                        """)
        Double getTotalRevenueByOrganizer(@Param("organizer") Organizer organizer);

        @Query("""
                        SELECT COALESCE(SUM(b.totalAmount), 0)
                        FROM Booking b
                        WHERE b.event = :event
                        """)
        Double getTotalRevenueByEvent(@Param("event") Event event);

        @Query("""
                        SELECT new map(
                            ts.type as type,
                            SUM(ts.quantity) as sold,
                            SUM(ts.totalPrice) as revenue
                        )
                        FROM Booking b
                        JOIN b.ticketsSummary ts
                        WHERE b.event = :event
                        GROUP BY ts.type
                        """)
        List<Map<String, Object>> getTicketTypeStatsByEvent(@Param("event") Event event);

        @Query("""
                        SELECT new map(
                            ts.type as type,
                            SUM(ts.quantity) as sold,
                            SUM(ts.totalPrice) as revenue
                        )
                        FROM Booking b
                        JOIN b.ticketsSummary ts
                        WHERE b.session = :session
                        GROUP BY ts.type
                        """)
        List<Map<String, Object>> getTicketTypeStatsBySession(@Param("session") Session session);

        List<Booking> findByEvent(Event event);

        @Query("""
                        SELECT new map(
                            e.category as category,
                            SUM(b.totalAmount) as revenue
                        )
                        FROM Booking b
                        JOIN b.event e
                        WHERE e.organizer = :organizer
                        GROUP BY e.category
                        """)
        List<Map<String, Object>> getRevenueByCategory(@Param("organizer") Organizer organizer);

        @Query("""
                        SELECT new map(
                            e.id as eventId,
                            e.title as title,
                            COALESCE(COUNT(bs), 0) as ticketsSold,
                            COALESCE(SUM(b.totalAmount), 0) as revenue
                        )
                        FROM Booking b
                        LEFT JOIN b.seats bs
                        JOIN b.event e
                        WHERE e.organizer = :organizer
                        GROUP BY e.id, e.title
                        ORDER BY ticketsSold DESC
                        LIMIT 3
                        """)
        List<Map<String, Object>> getTopSellingEvents(@Param("organizer") Organizer organizer);
}