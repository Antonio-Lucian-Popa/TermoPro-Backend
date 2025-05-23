package com.asusoftware.TermoPro.user.repository;

import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Caută un utilizator după UUID-ul din Keycloak.
     */
    Optional<User> findByKeycloakId(UUID keycloakId);

    /**
     * Verifică dacă un user cu rolul dat este deja înregistrat cu un anumit Keycloak ID.
     */
    boolean existsByKeycloakIdAndRole(UUID keycloakId, String role);

    Optional<User> findByEmail(String email);

    List<User> findAllByCompanyId(UUID companyId);

    List<User> findAllByCompanyIdAndRole(UUID companyId, UserRole role);

    @Query("SELECT u FROM User u WHERE u.companyId = :companyId AND u.role = 'OWNER'")
    User findOwnerByCompanyId(@Param("companyId") UUID companyId);

    boolean existsByIdAndCompanyIdAndRole(UUID id, UUID companyId, UserRole role);

    boolean existsByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByEmail(String email);

}
