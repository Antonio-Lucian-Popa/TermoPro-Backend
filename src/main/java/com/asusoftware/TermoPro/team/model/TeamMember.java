package com.asusoftware.TermoPro.team.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "team_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(TeamMemberId.class)
public class TeamMember {
    @Id
    private UUID teamId;

    @Id
    private UUID userId;

    @Column(nullable = false)
    private LocalDateTime joinedAt;
}
