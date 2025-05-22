package com.asusoftware.TermoPro.user_time_off.controller;

import com.asusoftware.TermoPro.user_time_off.model.dto.CreateTimeOffDto;
import com.asusoftware.TermoPro.user_time_off.model.dto.TimeOffDto;
import com.asusoftware.TermoPro.user_time_off.service.UserTimeOffService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/time-off")
@RequiredArgsConstructor
public class UserTimeOffController {

    private final UserTimeOffService service;

    @PostMapping
    public ResponseEntity<TimeOffDto> requestTimeOff(@RequestBody CreateTimeOffDto dto) {
        return ResponseEntity.ok(service.requestTimeOff(dto));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TimeOffDto>> getUserTimeOffs(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getTimeOffsForUser(userId));
    }

    @GetMapping("/by-date")
    public ResponseEntity<List<TimeOffDto>> getAllForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(service.getAllTimeOffsByDate(date));
    }

    @GetMapping("/user/{userId}/export/excel")
    public ResponseEntity<byte[]> exportExcel(@PathVariable UUID userId) {
        byte[] excel = service.exportTimeOffsToExcel(userId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=concedii_" + userId + ".xlsx")
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }


    @GetMapping("/pending/company/{companyId}")
    public ResponseEntity<List<TimeOffDto>> getPendingByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(service.getPendingRequestsForCompany(companyId));
    }

    @GetMapping("/user/{userId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID userId) {
        byte[] pdf = service.exportTimeOffsToPdf(userId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=concedii_" + userId + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }


    @PutMapping("/{requestId}/approve")
    public ResponseEntity<Void> approve(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt principal) {
        UUID keycloakId = UUID.fromString(principal.getSubject());
        service.approveRequest(requestId, keycloakId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{requestId}/reject")
    public ResponseEntity<Void> reject(@PathVariable UUID requestId, @AuthenticationPrincipal Jwt principal) {
        UUID keycloakId = UUID.fromString(principal.getSubject());
        service.rejectRequest(requestId, keycloakId);
        return ResponseEntity.noContent().build();
    }
}
