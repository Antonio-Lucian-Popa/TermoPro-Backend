package com.asusoftware.TermoPro.task.service;

import com.asusoftware.TermoPro.exception.ResourceNotFoundException;
import com.asusoftware.TermoPro.task.model.TaskUpdate;
import com.asusoftware.TermoPro.task.model.TaskUpdatePhoto;
import com.asusoftware.TermoPro.task.model.dto.CreateTaskUpdateDto;
import com.asusoftware.TermoPro.task.model.dto.TaskUpdateDto;
import com.asusoftware.TermoPro.task.repository.TaskRepository;
import com.asusoftware.TermoPro.task.repository.TaskUpdatePhotoRepository;
import com.asusoftware.TermoPro.task.repository.TaskUpdateRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskUpdateService {

    private final TaskUpdateRepository taskUpdateRepository;
    private final TaskUpdatePhotoRepository taskUpdatePhotoRepository;
    private final TaskRepository taskRepository;
    private final FileStorageService fileStorageService;
    private final ModelMapper mapper;

    @Transactional
    public TaskUpdateDto createTaskUpdate(CreateTaskUpdateDto dto, List<MultipartFile> images) {
        // Verificăm că task-ul există
        taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Taskul nu a fost găsit."));

        // Creăm și salvăm TaskUpdate
        TaskUpdate update = TaskUpdate.builder()
                .taskId(dto.getTaskId())
                .userId(dto.getUserId())
                .status(dto.getStatus())
                .comment(dto.getComment())
                .timestamp(LocalDateTime.now())
                .build();

        taskUpdateRepository.save(update);

        // Salvăm imaginile (dacă există)
        if (images != null) {
            for (MultipartFile image : images) {
                if (image.isEmpty()) continue;

                String imageUrl = fileStorageService.saveFile(image, update.getId());

                TaskUpdatePhoto photo = TaskUpdatePhoto.builder()
                        .taskUpdateId(update.getId())
                        .photoUrl(imageUrl)
                        .uploadedAt(LocalDateTime.now())
                        .build();

                taskUpdatePhotoRepository.save(photo);
            }
        }

        List<String> photoUrls = taskUpdatePhotoRepository.findAllByTaskUpdateId(update.getId()).stream()
                .map(TaskUpdatePhoto::getPhotoUrl)
                .collect(Collectors.toList());

        TaskUpdateDto result = mapper.map(update, TaskUpdateDto.class);
        result.setPhotoUrls(photoUrls);
        return result;
    }

    public List<TaskUpdateDto> getUpdatesByTask(UUID taskId) {
        return taskUpdateRepository.findAllByTaskIdOrderByTimestampAsc(taskId).stream()
                .map(update -> {
                    TaskUpdateDto dto = mapper.map(update, TaskUpdateDto.class);
                    List<String> urls = taskUpdatePhotoRepository.findAllByTaskUpdateId(update.getId())
                            .stream().map(TaskUpdatePhoto::getPhotoUrl).toList();
                    dto.setPhotoUrls(urls);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<TaskUpdatePhoto> getPhotosByUpdate(UUID updateId) {
        return taskUpdatePhotoRepository.findAllByTaskUpdateId(updateId);
    }
}