package com.asusoftware.TermoPro.user.controller;

import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.model.dto.CreateUserDto;
import com.asusoftware.TermoPro.user.model.dto.LoginDto;
import com.asusoftware.TermoPro.user.model.dto.UserDto;
import com.asusoftware.TermoPro.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ModelMapper mapper;

    /**
     * Înregistrare standard – doar OWNER / MANAGER poate crea useri manual.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody CreateUserDto dto) {
        UserDto created = userService.register(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Înregistrare pe bază de invitație.
     */
    @PostMapping("/register-invite")
    public ResponseEntity<UserDto> registerWithInvite(
            @RequestBody CreateUserDto dto,
            @RequestParam String token
    ) {
        UserDto created = userService.registerWithInvite(dto, token);
        return ResponseEntity.ok(created);
    }

    /**
     * Login user – întoarce tokenul Keycloak.
     */
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody LoginDto dto) {
        return ResponseEntity.ok(userService.login(dto));
    }

    /**
     * Returnează userul curent după keycloakId.
     */
    @GetMapping("/by-keycloak/{keycloakId}")
    public ResponseEntity<UserDto> getByKeycloakId(@PathVariable UUID keycloakId) {
        User user = userService.getByKeycloakId(keycloakId);
        return ResponseEntity.ok(mapper.map(user, UserDto.class));
    }

    /**
     * Șterge userul (hard delete).
     */
    @DeleteMapping("/by-keycloak/{keycloakId}")
    public ResponseEntity<Void> deleteByKeycloakId(@PathVariable UUID keycloakId) {
        userService.deleteByKeycloakId(keycloakId);
        return ResponseEntity.noContent().build();
    }
}
