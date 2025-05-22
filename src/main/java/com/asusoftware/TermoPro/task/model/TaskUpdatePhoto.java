package com.asusoftware.TermoPro.task.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_update_photo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskUpdatePhoto {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID taskUpdateId;

    @Column(nullable = false)
    private String photoUrl;

    private LocalDateTime uploadedAt = LocalDateTime.now();
}
