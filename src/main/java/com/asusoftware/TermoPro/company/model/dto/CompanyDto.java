package com.asusoftware.TermoPro.company.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {

    private UUID id;
    private String name;
    private LocalDateTime createdAt;
}
