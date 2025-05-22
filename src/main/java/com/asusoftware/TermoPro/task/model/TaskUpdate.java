package com.asusoftware.TermoPro.task.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_update")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskUpdate {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID taskId;
    private UUID userId;
    private String status;
    private String comment;
    private String photoUrl;
    private LocalDateTime timestamp = LocalDateTime.now();
}