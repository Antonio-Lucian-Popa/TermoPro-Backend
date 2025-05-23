package com.asusoftware.TermoPro.team.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    private UUID id;
    private String name;
    private UUID companyId;
    private LocalDateTime createdAt;
}
