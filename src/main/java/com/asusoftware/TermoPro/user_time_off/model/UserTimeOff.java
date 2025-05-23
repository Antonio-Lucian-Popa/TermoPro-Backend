package com.asusoftware.TermoPro.user_time_off.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "user_time_off")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTimeOff {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(value = EnumType.STRING)
    private TimeOffType type; // CONCEDIU, INVOIRE
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean approved;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalTime createdAt = LocalTime.now();
}
