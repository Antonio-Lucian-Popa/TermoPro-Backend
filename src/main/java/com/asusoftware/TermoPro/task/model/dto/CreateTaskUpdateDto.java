package com.asusoftware.TermoPro.task.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskUpdateDto {

    @NotNull
    private UUID taskId;

    @NotNull
    private UUID userId;

    @NotBlank
    private String status;

    private String comment;
}
