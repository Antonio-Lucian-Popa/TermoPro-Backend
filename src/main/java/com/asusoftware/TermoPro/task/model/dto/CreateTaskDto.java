package com.asusoftware.TermoPro.task.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskDto {
    private UUID orderId;
    private UUID teamId;
    private UUID userId;
    private String title;
    private String description;
    private String taskType;
    private LocalDate scheduledDate;
}
