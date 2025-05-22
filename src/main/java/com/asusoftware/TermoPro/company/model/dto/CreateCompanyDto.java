package com.asusoftware.TermoPro.company.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCompanyDto {

    @NotBlank
    private String name;
}
