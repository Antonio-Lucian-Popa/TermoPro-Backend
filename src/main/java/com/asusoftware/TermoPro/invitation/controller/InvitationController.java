package com.asusoftware.TermoPro.invitation.controller;

import com.asusoftware.TermoPro.company.service.CompanyService;
import com.asusoftware.TermoPro.invitation.model.dto.CreateInvitationDto;
import com.asusoftware.TermoPro.invitation.model.dto.InvitationDto;
import com.asusoftware.TermoPro.invitation.service.InvitationService;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final UserService userService;
    private final CompanyService companyService;

    /**
     * Creează o invitație nouă pentru o clinică (folosită pentru înregistrare).
     */
    @PostMapping
    public ResponseEntity<InvitationDto> createInvitation(@AuthenticationPrincipal Jwt principal,
                                                          @RequestBody CreateInvitationDto dto,
                                                          HttpServletRequest request) {
        UUID keycloakId = UUID.fromString(principal.getSubject());
        User creator = userService.getByKeycloakId(keycloakId);

        // Optional: verifici dacă userul este owner în clinică înainte de a permite crearea
        if (!companyService.isUserOwnerOfCompany(creator.getId(), dto.getCompanyId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();


        InvitationDto invitation = invitationService.generateInvitation(dto, baseUrl);
        return ResponseEntity.ok(invitation);
    }

    /**
     * Validează un token primit într-un link de invitație.
     * Frontend-ul poate apela acest endpoint înainte de înregistrare.
     */
    @GetMapping("/validate")
    public ResponseEntity<InvitationDto> validateToken(@RequestParam("token") String token) {
        InvitationDto invitation = invitationService.validateToken(token);
        return ResponseEntity.ok(invitation);
    }

    /**
     * Returnează invitațiile active pentru compania specificată.
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<InvitationDto>> getActiveInvitations(@PathVariable UUID companyId) {
        List<InvitationDto> invitations = invitationService.getActiveInvitations(companyId);
        return ResponseEntity.ok(invitations);
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> deleteInvitation(@PathVariable UUID invitationId,
                                                 @AuthenticationPrincipal Jwt principal) {
        UUID keycloakId = UUID.fromString(principal.getSubject());
        User user = userService.getByKeycloakId(keycloakId);

        // verifică dacă userul e owner într-o clinică cu această invitație
        if (!invitationService.canDeleteInvitation(invitationId, user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        invitationService.deleteInvitation(invitationId);
        return ResponseEntity.noContent().build();
    }

}