package com.asusoftware.TermoPro.task.controller;

import com.asusoftware.TermoPro.task.model.dto.CreateTaskDto;
import com.asusoftware.TermoPro.task.model.dto.TaskDto;
import com.asusoftware.TermoPro.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
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
    public ResponseEntity<List<TaskDto>> getAllByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(taskService.getAllForCompany(companyId));
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
     * Actualizează statusul unui task.
     */
    @PutMapping("/{taskId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable UUID taskId,
            @RequestParam String status,
            @RequestParam UUID companyId
    ) {
        taskService.updateTaskStatus(taskId, status, companyId);
        return ResponseEntity.noContent().build();
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
