package com.asusoftware.TermoPro.invitation.model.dto;

import com.asusoftware.TermoPro.user.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvitationDto {
    private String employeeEmail; // email of the user to invite
    private UUID companyId;
    private UserRole role;      // TEHNICIAN / etc...
}
