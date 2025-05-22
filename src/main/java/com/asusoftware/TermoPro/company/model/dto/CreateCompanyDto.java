package com.asusoftware.TermoPro.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCompanyDto {

    @NotBlank
    private String name;
    private UUID ownerId;
}
