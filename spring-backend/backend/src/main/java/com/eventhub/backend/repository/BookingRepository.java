package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.entity.Event;
import com.eventhub.backend.entity.Session;
import com.eventhub.backend.entity.User;
import com.eventhub.backend.entity.Organizer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface BookingRepository
                extends JpaRepository<Booking, Long> {

        @EntityGraph(attributePaths = {"event", "session"})
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
                        SELECT new com.eventhub.backend.dto.TicketTypeStatsDTO(
                            ts.type,
                            SUM(ts.quantity),
                            SUM(ts.totalPrice)
                        )
                        FROM Booking b
                        JOIN b.ticketsSummary ts
                        WHERE b.event = :event
                        GROUP BY ts.type
                        """)
        List<com.eventhub.backend.dto.TicketTypeStatsDTO> getTicketTypeStatsByEvent(@Param("event") Event event);

        @Query("""
                        SELECT new com.eventhub.backend.dto.TicketTypeStatsDTO(
                            ts.type,
                            SUM(ts.quantity),
                            SUM(ts.totalPrice)
                        )
                        FROM Booking b
                        JOIN b.ticketsSummary ts
                        WHERE b.session = :session
                        GROUP BY ts.type
                        """)
        List<com.eventhub.backend.dto.TicketTypeStatsDTO> getTicketTypeStatsBySession(@Param("session") Session session);

        List<Booking> findByEvent(Event event);

        @Query("""
                        SELECT new com.eventhub.backend.dto.CategoryRevenueDTO(
                            e.category,
                            SUM(b.totalAmount)
                        )
                        FROM Booking b
                        JOIN b.event e
                        WHERE e.organizer = :organizer
                        GROUP BY e.category
                        """)
        List<com.eventhub.backend.dto.CategoryRevenueDTO> getRevenueByCategory(@Param("organizer") Organizer organizer);

        @Query("""
                        SELECT new com.eventhub.backend.dto.TopSellingEventDTO(
                            e.id,
                            e.title,
                            COALESCE(COUNT(bs), 0),
                            COALESCE(SUM(b.totalAmount), 0)
                        )
                        FROM Booking b
                        LEFT JOIN b.seats bs
                        JOIN b.event e
                        WHERE e.organizer = :organizer
                        GROUP BY e.id, e.title
                        ORDER BY COALESCE(COUNT(bs), 0) DESC
                        LIMIT 3
                        """)
        List<com.eventhub.backend.dto.TopSellingEventDTO> getTopSellingEvents(@Param("organizer") Organizer organizer);

        @Query("""
                        SELECT new com.eventhub.backend.dto.SessionTicketStatsDTO(
                            s,
                            ts.type,
                            SUM(ts.quantity),
                            SUM(ts.totalPrice)
                        )
                        FROM Booking b
                        JOIN b.session s
                        JOIN b.ticketsSummary ts
                        WHERE s.event = :event
                        GROUP BY s, ts.type
                        """)
        List<com.eventhub.backend.dto.SessionTicketStatsDTO> getAllSessionTicketStatsByEvent(@Param("event") Event event);
}