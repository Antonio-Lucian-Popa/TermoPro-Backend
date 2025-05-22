package com.asusoftware.TermoPro.task.controller;

import com.asusoftware.TermoPro.task.model.TaskUpdatePhoto;
import com.asusoftware.TermoPro.task.model.dto.CreateTaskUpdateDto;
import com.asusoftware.TermoPro.task.model.dto.TaskUpdateDto;
import com.asusoftware.TermoPro.task.service.TaskUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/task-updates")
@RequiredArgsConstructor
public class TaskUpdateController {

    private final TaskUpdateService taskUpdateService;

    /**
     * Creează un update pentru un task, cu status, comentariu și imagini atașate.
     */
    @PostMapping
    public ResponseEntity<TaskUpdateDto> createUpdate(
            @RequestPart("data") @Valid CreateTaskUpdateDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        TaskUpdateDto update = taskUpdateService.createTaskUpdate(dto, images);
        return ResponseEntity.ok(update);
    }

    /**
     * Returnează toate update-urile pentru un task.
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<TaskUpdateDto>> getAllForTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskUpdateService.getUpdatesByTask(taskId));
    }

    /**
     * Returnează toate pozele pentru un anumit update.
     */
    @GetMapping("/{updateId}/photos")
    public ResponseEntity<List<TaskUpdatePhoto>> getPhotosForUpdate(@PathVariable UUID updateId) {
        return ResponseEntity.ok(taskUpdateService.getPhotosByUpdate(updateId));
    }
}
