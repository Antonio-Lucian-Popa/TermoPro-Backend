package com.asusoftware.TermoPro.team.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UpdateTeamDto {
    private String name;
    private UUID requesterId;
}
