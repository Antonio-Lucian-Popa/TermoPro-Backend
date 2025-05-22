package com.asusoftware.TermoPro.team.repository;

import com.asusoftware.TermoPro.team.model.TeamMember;
import com.asusoftware.TermoPro.team.model.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamMembersRepository extends JpaRepository<TeamMember, TeamMemberId> {

    // Găsește toți userii dintr-o echipă
    List<TeamMember> findAllByTeamId(UUID teamId);

    // Găsește toate echipele unui utilizator
    List<TeamMember> findAllByUserId(UUID userId);

    // Verifică dacă un utilizator este într-o echipă
    boolean existsByTeamIdAndUserId(UUID teamId, UUID userId);

    // Șterge toate legăturile pentru un user (ex: când este eliminat din companie)
    void deleteByUserId(UUID userId);

    // Șterge toate legăturile pentru o echipă (ex: când este ștearsă echipa)
    void deleteByTeamId(UUID teamId);
}
