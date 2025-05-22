package com.asusoftware.TermoPro.task.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskUpdateDto {
    private UUID id;
    private UUID taskId;
    private UUID userId;
    private String status;
    private String comment;
    private LocalDateTime timestamp;

    private List<String> photoUrls; // link-urile imaginilor asociate
}
