package com.asusoftware.TermoPro.invitation.repository;

import com.asusoftware.TermoPro.invitation.model.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    /**
     * Caută o invitație după tokenul său (UUID sau string generat securizat).
     */
    Optional<Invitation> findByToken(String token);

    /**
     * Verifică dacă o invitație există deja pentru un anumit company + rol
     * (poate fi util dacă vrei să eviți trimiterea multiplă).
     */
    boolean existsByCompanyIdAndRole(UUID companyId, String role);

    /**
     * Returnează toate invitațiile active (nefolosite și neexpirate) pentru o clinică.
     */
    @Query("SELECT i FROM Invitation i WHERE i.companyId = :companyId AND i.used = false AND i.expiresAt > CURRENT_TIMESTAMP")
    List<Invitation> findActiveInvitationsByCompany(@Param("companyId") UUID companyId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Invitation i WHERE i.companyId = :companyId AND i.employeeEmail = :employeeEmail")
    void deleteByCompanyIdAndEmployeeEmail(@Param("companyId") UUID companyId, @Param("employeeEmail") String employeeEmail);

}
