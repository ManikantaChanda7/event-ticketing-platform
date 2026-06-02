package com.eventhub.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

import com.eventhub.backend.enums.BookingStatus;

@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ElementCollection
    @CollectionTable(name = "booking_seats_embedded", joinColumns = @JoinColumn(name = "booking_id"))
    @AttributeOverride(name = "seatId", column = @Column(name = "seat_id", columnDefinition = "VARCHAR(255)"))
    @AttributeOverride(name = "section", column = @Column(name = "section", columnDefinition = "VARCHAR(255)"))
    @AttributeOverride(name = "price", column = @Column(name = "price"))
    private List<BookedSeat> seats;

    @ElementCollection
    @CollectionTable(name = "booking_tickets_summary", joinColumns = @JoinColumn(name = "booking_id"))
    @AttributeOverride(name = "type", column = @Column(name = "type"))
    @AttributeOverride(name = "quantity", column = @Column(name = "quantity"))
    @AttributeOverride(name = "totalPrice", column = @Column(name = "total_price"))
    private List<TicketSummary> ticketsSummary;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @Embeddable
    public static class BookedSeat {
        private String seatId;
        private String section;
        private BigDecimal price;

        public BookedSeat() {
        }

        public BookedSeat(String seatId, String section, BigDecimal price) {
            this.seatId = seatId;
            this.section = section;
            this.price = price;
        }

        public String getSeatId() {
            return seatId;
        }

        public void setSeatId(String seatId) {
            this.seatId = seatId;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }

    @Embeddable
    public static class TicketSummary {
        private String type;
        private Integer quantity;
        private BigDecimal totalPrice;

        public TicketSummary() {
        }

        public TicketSummary(String type, Integer quantity, BigDecimal totalPrice) {
            this.type = type;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
        }
    }
}