package com.asusoftware.TermoPro.invitation.service;

import com.asusoftware.TermoPro.company.model.dto.CompanyDto;
import com.asusoftware.TermoPro.company.service.CompanyService;
import com.asusoftware.TermoPro.exception.InvalidTokenException;
import com.asusoftware.TermoPro.invitation.model.Invitation;
import com.asusoftware.TermoPro.invitation.model.dto.CreateInvitationDto;
import com.asusoftware.TermoPro.invitation.model.dto.InvitationDto;
import com.asusoftware.TermoPro.invitation.repository.InvitationRepository;
import com.asusoftware.TermoPro.mail.service.MailService;
import com.asusoftware.TermoPro.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserService userService;
    private final MailService mailService;
    private final CompanyService companyService;
    private final ModelMapper mapper;

    /**
     * Generează o invitație unică pentru o clinică și un rol.
     */
    @Transactional
    public InvitationDto generateInvitation(CreateInvitationDto dto, String baseUrl) {
        String token = UUID.randomUUID().toString();

        // trbeuie sa luam clinicId si doctorId
        CompanyDto companyDto = companyService.getById(dto.getCompanyId());

        Invitation invitation = Invitation.builder()
                .token(token)
                .companyId(dto.getCompanyId())
                .role(dto.getRole())
                .employeeEmail(dto.getEmployeeEmail())
                .expiresAt(LocalDateTime.now().plusDays(4)) // valabil 7 zile
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        invitationRepository.save(invitation);

        // Trebuie sa trimitem email la user cu token(adica invitatia)
        // 2. Trimiți mail
        String link = "http://localhost:5173" + "/register/invite?token=" + invitation.getToken();
        mailService.sendInvitationEmail(dto.getEmployeeEmail(), link, companyDto.getName(), dto.getRole().toString());

        return mapper.map(invitation, InvitationDto.class);
    }

    /**
     * Validează tokenul și returnează datele legate de invitație.
     */
    public InvitationDto validateToken(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invitație invalidă"));

        if (invitation.getUsed()) {
            throw new InvalidTokenException("Invitația a fost deja folosită");
        }

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Invitația a expirat");
        }

        return mapper.map(invitation, InvitationDto.class);
    }

    /**
     * Marchează invitația ca folosită după înregistrare.
     */
    @Transactional
    public void markAsUsed(String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invitație inexistentă"));

        invitation.setUsed(true);
        invitationRepository.save(invitation);
    }

    /**
     * Opțional: returnează invitațiile active pentru o clinică.
     */
    public List<InvitationDto> getActiveInvitations(UUID companyId) {
        return invitationRepository.findActiveInvitationsByCompany(companyId).stream()
                .map(i -> mapper.map(i, InvitationDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteInvitation(UUID invitationId) {
        invitationRepository.deleteById(invitationId);
    }

    public boolean canDeleteInvitation(UUID invitationId, UUID userId) {
        return invitationRepository.findById(invitationId)
                .map(inv -> companyService.isUserOwnerOfCompany(userId, inv.getCompanyId()))
                .orElse(false);
    }

}