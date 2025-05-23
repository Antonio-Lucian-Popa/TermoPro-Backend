package com.asusoftware.TermoPro.task.controller;

import com.asusoftware.TermoPro.task.model.dto.CreateTaskDto;
import com.asusoftware.TermoPro.task.model.dto.TaskDto;
import com.asusoftware.TermoPro.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Creează un task nou – poate fi atribuit unei echipe sau unui utilizator individual.
     */
    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            @RequestBody @Valid CreateTaskDto dto,
            @RequestParam UUID creatorUserId
    ) {
        TaskDto task = taskService.createTask(dto, creatorUserId);
        return ResponseEntity.ok(task);
    }

    /**
     * Returnează toate taskurile dintr-o companie.
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<TaskDto>> getAllByCompany(
            @PathVariable UUID companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("scheduledDate").descending());
        return ResponseEntity.ok(taskService.getAllForCompany(companyId, status, type, pageable));
    }

    /**
     * Returnează toate taskurile programate pentru o zi într-o companie.
     */
    @GetMapping("/company/{companyId}/date/{date}")
    public ResponseEntity<List<TaskDto>> getByDate(
            @PathVariable UUID companyId,
            @PathVariable LocalDate date
    ) {
        return ResponseEntity.ok(taskService.getTasksByDateAndCompany(date, companyId));
    }

    /**
     * Returnează taskurile unei echipe.
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TaskDto>> getByTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(taskService.getAllForTeam(teamId));
    }

    /**
     * Returnează taskurile atribuite unui utilizator individual.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskDto>> getByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.getAllForUser(userId));
    }

    /**
     * Exportă taskurile unei echipe într-un PDF.
     * Se poate filtra opțional după dată.
     */
    @GetMapping("/team/{teamId}/export")
    public ResponseEntity<byte[]> exportTasksForTeam(
            @PathVariable UUID teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        byte[] pdf = taskService.exportTasksWithUpdatesToPdf(teamId, date);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=raport_taskuri_echipa.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Exportă taskurile unui user individual într-un PDF.
     * Se poate filtra opțional după dată.
     */
    @GetMapping("/user/{userId}/export")
    public ResponseEntity<byte[]> exportTasksForUser(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        byte[] pdf = taskService.exportUserTasksWithUpdatesToPdf(userId, date);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=raport_taskuri_user.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/team/{teamId}/export/excel")
    public ResponseEntity<byte[]> exportExcelTeam(
            @PathVariable UUID teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        byte[] excel = taskService.exportTasksWithUpdatesToExcel(teamId, date);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=taskuri_echipa.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/user/{userId}/export/excel")
    public ResponseEntity<byte[]> exportExcelUser(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        byte[] excel = taskService.exportUserTasksWithUpdatesToExcel(userId, date);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=taskuri_user.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    /**
     * Returnează detalii despre un task după ID.
     */
    @GetMapping("/{companyId}/{taskId}")
    public ResponseEntity<TaskDto> getTaskDetails(@PathVariable(name = "taskId") UUID taskId, @PathVariable(name = "companyId") UUID companyId) {
        return ResponseEntity.ok(taskService.getById(taskId, companyId));
    }



    /**
     * Actualizează statusul unui task.
     */
    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskDto> updateStatus(
            @PathVariable UUID taskId,
            @RequestParam String status,
            @RequestParam UUID companyId
    ) {
        TaskDto task = taskService.updateTaskStatus(taskId, status, companyId);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            @RequestParam UUID userId // ID-ul celui care face cererea
    ) {
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.noContent().build();
    }

}
