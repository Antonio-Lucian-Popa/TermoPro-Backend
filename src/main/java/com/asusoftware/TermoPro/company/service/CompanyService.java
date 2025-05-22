package com.asusoftware.TermoPro.company.service;

import com.asusoftware.TermoPro.company.model.Company;
import com.asusoftware.TermoPro.company.model.dto.CompanyDto;
import com.asusoftware.TermoPro.company.model.dto.CreateCompanyDto;
import com.asusoftware.TermoPro.company.repository.CompanyRepository;
import com.asusoftware.TermoPro.exception.CompanyAlreadyExistsException;
import com.asusoftware.TermoPro.exception.ResourceNotFoundException;
import com.asusoftware.TermoPro.invitation.repository.InvitationRepository;
import com.asusoftware.TermoPro.team.repository.TeamMembersRepository;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.model.UserRole;
import com.asusoftware.TermoPro.user.model.dto.UserDto;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final ModelMapper mapper;

    /**
     * Creează o companie nouă dacă numele nu există deja.
     */
    @Transactional
    public CompanyDto createCompany(CreateCompanyDto dto) {
        User user = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));
        if (companyRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new CompanyAlreadyExistsException("Există deja o companie cu acest nume.");
        }

        Company company = Company.builder()
                .name(dto.getName())
                .createdAt(LocalDateTime.now())
                .build();

        companyRepository.save(company);
        user.setCompanyId(company.getId());
        userRepository.save(user);
        return mapper.map(company, CompanyDto.class);
    }

    /**
     * Returnează compania după ID.
     */
    public CompanyDto getById(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Compania nu a fost găsită."));
        return mapper.map(company, CompanyDto.class);
    }

    /**
     * Verifică dacă un user este OWNER într-o companie.
     */
    public boolean isUserOwnerOfCompany(UUID userId, UUID companyId) {
        return userRepository.existsByIdAndCompanyIdAndRole(userId, companyId, UserRole.OWNER);
    }

    /**
     * Verifică dacă un user aparține unei companii.
     */
    public boolean isUserInCompany(UUID userId, UUID companyId) {
        return userRepository.existsByIdAndCompanyId(userId, companyId);
    }

    /**
     * Returnează toți utilizatorii din companie.
     */
    public List<UserDto> getUsersInCompany(UUID companyId) {
        return userRepository.findAllByCompanyId(companyId).stream()
                .map(user -> mapper.map(user, UserDto.class))
                .toList();
    }

    /**
     * Scoate un utilizator din companie + curăță date asociate.
     */
    @Transactional
    public void removeUserFromCompany(UUID companyId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit."));

        if (!companyId.equals(user.getCompanyId())) {
            throw new IllegalArgumentException("Utilizatorul nu aparține companiei specificate.");
        }

        // 1. Scoate userul din companie
        user.setCompanyId(null);
        userRepository.save(user);

        // 2. Șterge invitațiile nefolosite
        invitationRepository.deleteByCompanyIdAndEmployeeEmail(companyId, user.getEmail());

        // 3. Șterge legătura din echipe
        teamMembersRepository.deleteByUserId(userId);
    }

}
