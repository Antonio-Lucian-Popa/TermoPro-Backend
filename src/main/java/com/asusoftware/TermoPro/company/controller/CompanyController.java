package com.asusoftware.TermoPro.company.controller;

import com.asusoftware.TermoPro.company.model.dto.CompanyDto;
import com.asusoftware.TermoPro.company.model.dto.CreateCompanyDto;
import com.asusoftware.TermoPro.company.service.CompanyService;
import com.asusoftware.TermoPro.user.model.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Creează o companie nouă.
     * Doar userii cu rol OWNER ar trebui să poată face asta (logică de frontend/autorizare).
     */
    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(
            @RequestBody CreateCompanyDto dto
    ) {
        CompanyDto created = companyService.createCompany(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * Returnează o companie după ID.
     */
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyService.getById(companyId));
    }

    /**
     * Returnează lista utilizatorilor dintr-o companie.
     */
    @GetMapping("/{companyId}/users")
    public ResponseEntity<List<UserDto>> getUsersInCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(companyService.getUsersInCompany(companyId));
    }

    /**
     * Șterge un utilizator dintr-o companie.
     * Doar OWNER ar trebui să poată apela (verificat prin frontend sau interceptor).
     */
    @DeleteMapping("/{companyId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromCompany(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {
        companyService.removeUserFromCompany(companyId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifică dacă un user este OWNER într-o companie.
     */
    @GetMapping("/{companyId}/is-owner")
    public ResponseEntity<Boolean> isUserOwner(
            @PathVariable UUID companyId,
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(companyService.isUserOwnerOfCompany(userId, companyId));
    }

    /**
     * Verifică dacă un user aparține companiei.
     */
    @GetMapping("/{companyId}/is-member")
    public ResponseEntity<Boolean> isUserInCompany(
            @PathVariable UUID companyId,
            @RequestParam UUID userId
    ) {
        return ResponseEntity.ok(companyService.isUserInCompany(userId, companyId));
    }
}