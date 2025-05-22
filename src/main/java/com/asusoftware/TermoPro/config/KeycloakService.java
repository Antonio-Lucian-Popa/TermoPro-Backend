package com.asusoftware.TermoPro.config;

import com.asusoftware.TermoPro.exception.UserAlreadyExistsException;
import com.asusoftware.TermoPro.user.model.dto.CreateUserDto;
import com.asusoftware.TermoPro.user.model.dto.LoginDto;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    public String createKeycloakUser(CreateUserDto userDTO) {
        Keycloak keycloak = getKeycloakAdminInstance();

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRealmRoles(Collections.singletonList(userDTO.getRole().name()));

        CredentialRepresentation password = new CredentialRepresentation();
        password.setType(CredentialRepresentation.PASSWORD);
        password.setTemporary(false);
        password.setValue(userDTO.getPassword());
        user.setCredentials(Collections.singletonList(password));

        Response response = keycloak.realm(realm).users().create(user);
        if (response.getStatus() == 201) {
            String location = response.getHeaderString("Location");
            return location.substring(location.lastIndexOf('/') + 1);
        } else if (response.getStatus() == 409) {
            throw new UserAlreadyExistsException("Un cont cu acest email există deja.");
        }
        else {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatus());
        }
    }

    public AccessTokenResponse loginUser(LoginDto loginDto) {
        try {
            return obtainToken(loginDto.getEmail(), loginDto.getPassword());
        } catch (Exception e) {
            throw new RuntimeException("Failed to login user: " + e.getMessage(), e);
        }
    }

    public AccessTokenResponse refreshToken(String refreshToken) {
        try {
            var client = javax.ws.rs.client.ClientBuilder.newClient();
            var target = client.target(authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token");

            var form = new javax.ws.rs.core.Form();
            form.param("grant_type", "refresh_token");
            form.param("client_id", clientId);
            form.param("client_secret", clientSecret);
            form.param("refresh_token", refreshToken);

            return target.request()
                    .post(javax.ws.rs.client.Entity.form(form), AccessTokenResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage(), e);
        }
    }


    public void deleteKeycloakUser(UUID keycloakId) {
        Keycloak keycloak = getKeycloakAdminInstance();
        keycloak.realm(realm).users().delete(String.valueOf(keycloakId));
    }

    private AccessTokenResponse obtainToken(String username, String password) {
        System.out.println("Obtaining token for user: " + authServerUrl + " Password: " + clientId + " in realm: " + clientSecret);
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build()
                .tokenManager()
                .getAccessToken();
    }

    private Keycloak getKeycloakAdminInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

}
