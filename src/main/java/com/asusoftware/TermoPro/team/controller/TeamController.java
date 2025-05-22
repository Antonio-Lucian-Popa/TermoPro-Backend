package com.asusoftware.TermoPro.team.controller;

import com.asusoftware.TermoPro.team.model.dto.TeamDto;
import com.asusoftware.TermoPro.team.service.TeamService;
import com.asusoftware.TermoPro.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    /**
     * Creează o echipă nouă într-o companie (OWNER / MANAGER doar).
     */
    @PostMapping
    public ResponseEntity<TeamDto> createTeam(
            @RequestParam UUID companyId,
            @RequestParam UUID requesterId,
            @RequestParam String name
    ) {
        TeamDto team = teamService.createTeam(name, companyId, requesterId);
        return ResponseEntity.ok(team);
    }

    /**
     * Adaugă un utilizator într-o echipă (OWNER / MANAGER doar).
     */
    @PostMapping("/{teamId}/members")
    public ResponseEntity<Void> addUserToTeam(
            @PathVariable UUID teamId,
            @RequestParam UUID userId,
            @RequestParam UUID requesterId
    ) {
        teamService.addUserToTeam(teamId, userId, requesterId);
        return ResponseEntity.ok().build();
    }

    /**
     * Elimină un utilizator dintr-o echipă (OWNER / MANAGER doar).
     */
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<Void> removeUserFromTeam(
            @PathVariable UUID teamId,
            @PathVariable UUID userId,
            @RequestParam UUID requesterId
    ) {
        teamService.removeUserFromTeam(teamId, userId, requesterId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returnează toți userii dintr-o echipă.
     */
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<UserDto>> getUsersInTeam(
            @PathVariable UUID teamId
    ) {
        return ResponseEntity.ok(teamService.getUsersInTeam(teamId));
    }

    /**
     * Verifică dacă un user este membru al unei echipe.
     */
    @GetMapping("/{teamId}/members/{userId}/check")
    public ResponseEntity<Boolean> isUserInTeam(
            @PathVariable UUID teamId,
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(teamService.isUserInTeam(teamId, userId));
    }
}