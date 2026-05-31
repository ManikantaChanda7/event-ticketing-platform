package com.eventhub.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sessions")
@Getter
@Setter
public class Session extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date; // Changed from sessionDate to match Node.js

    private String startTime; // Changed from LocalTime to String to match Node.js "HH:mm"

    private String endTime; // Changed from LocalTime to String to match Node.js "HH:mm"

    private LocalDateTime releaseDate; // Added to match Node.js

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ElementCollection
    @CollectionTable(name = "session_tickets", joinColumns = @JoinColumn(name = "session_id"))
    private List<Ticket> tickets;

    @OneToMany(mappedBy = "session")
    private List<Seat> seats;

    private Integer occupancy = 0; // Added to match Node.js

    @Embeddable
    public static class Ticket {
        private String type; // "Gold", "Silver", "Platinum"
        private Double price;
        private Integer available;
        private Integer totalSeats;

        public Ticket() {
        }

        public Ticket(String type, Double price, Integer available, Integer totalSeats) {
            this.type = type;
            this.price = price;
            this.available = available;
            this.totalSeats = totalSeats;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Integer getAvailable() {
            return available;
        }

        public void setAvailable(Integer available) {
            this.available = available;
        }

        public Integer getTotalSeats() {
            return totalSeats;
        }

        public void setTotalSeats(Integer totalSeats) {
            this.totalSeats = totalSeats;
        }
    }
}