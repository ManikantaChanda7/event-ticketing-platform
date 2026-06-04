package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Seat;
import com.eventhub.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query("SELECT s FROM Seat s WHERE s.seatId = :seatId AND s.session.id = :sessionId")
    List<Seat> findAllBySeatIdAndSessionId(@Param("seatId") String seatId, @Param("sessionId") Long sessionId);

    List<Seat> findBySession(Session session);
}
