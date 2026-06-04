package com.eventhub.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "ticket_types", indexes = {
        @Index(name = "idx_ticket_session", columnList = "session_id")
})
@Getter
@Setter
public class TicketType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    private Integer quantity;

    private Integer remainingQuantity;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;
}