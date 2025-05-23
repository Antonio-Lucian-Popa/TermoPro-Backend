package com.asusoftware.TermoPro.user.service;

import com.asusoftware.TermoPro.config.KeycloakService;
import com.asusoftware.TermoPro.exception.InvalidTokenException;
import com.asusoftware.TermoPro.exception.UserAlreadyExistsException;
import com.asusoftware.TermoPro.exception.UserNotFoundException;
import com.asusoftware.TermoPro.invitation.model.Invitation;
import com.asusoftware.TermoPro.invitation.repository.InvitationRepository;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.model.dto.CreateUserDto;
import com.asusoftware.TermoPro.user.model.dto.LoginDto;
import com.asusoftware.TermoPro.user.model.dto.UserDto;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final KeycloakService keycloakService;
    private final ModelMapper mapper;

    /**
     * Înregistrare clasică – Owner sau Manager creează conturi din aplicație.
     */
    @Transactional
    public UserDto register(CreateUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("Există deja un utilizator cu acest email.");
        }

        String keycloakId = keycloakService.createKeycloakUser(dto);

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .keycloakId(UUID.fromString(keycloakId))
                .role(dto.getRole())
                .companyId(dto.getCompanyId())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return mapper.map(user, UserDto.class);
    }

    /**
     * Înregistrare cu invitație – doar tokenul conține informațiile sensibile.
     */
    @Transactional
    public UserDto registerWithInvite(CreateUserDto dto, String token) {
        Invitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invitația nu este validă."));

        if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Invitația a expirat.");
        }

        Optional<User> existingUserOpt = userRepository.findByEmail(dto.getEmail());

        if (existingUserOpt.isPresent()) {
            throw new UserAlreadyExistsException("Utilizatorul există deja. Conectează-te direct.");
        }

        String keycloakId = keycloakService.createKeycloakUser(dto);

        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .keycloakId(UUID.fromString(keycloakId))
                .role(invitation.getRole())
                .companyId(invitation.getCompanyId())
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        invitation.setUsed(true);
        invitationRepository.save(invitation);

        return mapper.map(user, UserDto.class);
    }

    /**
     * Returnează userul autenticat după keycloakId.
     */
    public User getByKeycloakId(UUID keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException("User cu keycloakId " + keycloakId + " nu a fost găsit"));
    }

    /**
     * Returnează userul după id-ul din aplicație.
     */
    public UserDto getById(UUID id) {
        return userRepository.findById(id)
                .map(user -> mapper.map(user, UserDto.class))
                .orElseThrow(() -> new UserNotFoundException("User cu id " + id + " nu a fost găsit"));
    }

    /**
     * Login utilizator prin Keycloak.
     */
    public AccessTokenResponse login(LoginDto dto) {
        return keycloakService.loginUser(dto);
    }

    /**
     * Ștergere cont după keycloakId (soft delete sau hard).
     */
    @Transactional
    public void deleteByKeycloakId(UUID keycloakId) {
        userRepository.findByKeycloakId(keycloakId)
                .ifPresent(userRepository::delete);
    }
}

