package com.asusoftware.TermoPro.company.service;

import com.asusoftware.TermoPro.company.model.Company;
import com.asusoftware.TermoPro.company.model.dto.CompanyDto;
import com.asusoftware.TermoPro.company.model.dto.CreateCompanyDto;
import com.asusoftware.TermoPro.company.repository.CompanyRepository;
import com.asusoftware.TermoPro.exception.CompanyAlreadyExistsException;
import com.asusoftware.TermoPro.invitation.repository.InvitationRepository;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.model.UserRole;
import com.asusoftware.TermoPro.user.model.dto.UserDto;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;
    private final InvitationRepository invitationRepository;

    @Transactional
    public CompanyDto createCompany(CreateCompanyDto dto) {
        if (companyRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new CompanyAlreadyExistsException("Există deja o companie cu acest nume.");
        }

        Company company = Company.builder()
                .name(dto.getName())
                .createdAt(java.time.LocalDateTime.now())
                .build();

        companyRepository.save(company);

        return mapper.map(company, CompanyDto.class);
    }

    public CompanyDto getById(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Compania nu a fost găsită."));
        return mapper.map(company, CompanyDto.class);
    }

    /**
     * Verifică dacă un user este owner într-o companie.
     */
    public boolean isUserOwnerOfCompany(UUID userId, UUID companyId) {
        return userRepository.existsByIdAndCompanyIdAndRole(userId, companyId, UserRole.OWNER);
    }

    public boolean isUserInCompany(UUID userId, UUID companyId) {
        return userRepository.existsByIdAndCompanyId(userId, companyId);
    }

    public List<UserDto> getUsersInCompany(UUID companyId) {
        List<User> users = userRepository.findAllByCompanyId(companyId);
        return users.stream().map(user -> mapper.map(user, UserDto.class)).toList();
    }

    @Transactional
    public void removeUserFromCompany(UUID companyId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!companyId.equals(user.getCompanyId())) {
            throw new IllegalArgumentException("Utilizatorul nu aparține companiei specificate.");
        }

        // 1. Scoatem userul din companie
        user.setCompanyId(null);
        userRepository.save(user);

        // 2. Ștergem eventualele invitații restante (dacă există)
        invitationRepository.deleteByCompanyIdAndEmployeeEmail(companyId, user.getEmail());

        // 3. (Opțional) Ștergem din team_members
        teamMembersRepository.deleteByUserId(userId);
    }

}
