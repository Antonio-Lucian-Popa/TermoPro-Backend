package com.asusoftware.TermoPro.customer_order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerOrder {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private String clientPhone;

    @Column(nullable = false)
    private String clientAddress;
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status; // PENDING, IN_PROGRESS, DONE, CANCELLED

    @Column(nullable = false)
    private UUID companyId;

    private UUID teamId;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
