package com.asusoftware.TermoPro.task.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID orderId;

    private UUID teamId; // nullable dacă e individual
    private UUID userId; // nullable dacă e pentru echipă

    @Column(nullable = false)
    private String title;

    private String description;
    private String taskType;
    private String status;
    private LocalDate scheduledDate;
    private UUID companyId;

    @Column(nullable = false)
    private UUID assignedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
