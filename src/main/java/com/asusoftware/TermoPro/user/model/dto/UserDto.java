package com.asusoftware.TermoPro.user.model.dto;


import com.asusoftware.TermoPro.user.model.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String keycloakId;
    private UUID companyId;
    private LocalDateTime createdAt;
}
