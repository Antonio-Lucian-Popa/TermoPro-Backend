package com.asusoftware.TermoPro.team.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TeamDto {
    private UUID id;
    private String name;
    private UUID companyId;
}
