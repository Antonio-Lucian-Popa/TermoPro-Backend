package com.asusoftware.TermoPro.company.repository;

import com.asusoftware.TermoPro.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByNameIgnoreCase(String name);
}