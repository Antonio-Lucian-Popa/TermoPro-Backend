package com.asusoftware.TermoPro.invitation.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDto {
    private UUID id;
    private String token;
    private UUID companyId;
    private String role;
    private String employeeEmail;
    private LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime createdAt;
}
