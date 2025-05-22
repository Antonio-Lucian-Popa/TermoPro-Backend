package com.asusoftware.TermoPro.team.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateTeamDto {
    private String name;
    private UUID companyId;
    private UUID requesterId;
}
