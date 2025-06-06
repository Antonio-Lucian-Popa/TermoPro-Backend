package com.asusoftware.TermoPro.team.repository;

import com.asusoftware.TermoPro.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findAllByCompanyId(UUID companyId);
}
