package com.eventhub.backend.repository;

import com.eventhub.backend.entity.Booking;
import com.eventhub.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository
        extends JpaRepository<Booking, Long> {

    List<Booking> findByUser(User user);
}