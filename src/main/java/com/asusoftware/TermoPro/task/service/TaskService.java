package com.asusoftware.TermoPro.task.service;

import com.asusoftware.TermoPro.exception.ResourceNotFoundException;
import com.asusoftware.TermoPro.task.model.Task;
import com.asusoftware.TermoPro.task.model.dto.CreateTaskDto;
import com.asusoftware.TermoPro.task.model.dto.TaskDto;
import com.asusoftware.TermoPro.task.repository.TaskRepository;
import com.asusoftware.TermoPro.task.repository.TaskUpdatePhotoRepository;
import com.asusoftware.TermoPro.task.repository.TaskUpdateRepository;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskUpdateRepository taskUpdateRepository;
    private final TaskUpdatePhotoRepository taskUpdatePhotoRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    @Transactional
    public TaskDto createTask(CreateTaskDto dto, UUID creatorUserId) {
        if (dto.getTeamId() == null && dto.getUserId() == null) {
            throw new IllegalArgumentException("Trebuie să specifici fie teamId, fie userId.");
        }

        // Verifica daca user are rol de admin in companie
        User userCreator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit"));

        if (!userCreator.getRole().equals("OWNER") && !userCreator.getRole().equals("ADMIN")) {
            throw new IllegalArgumentException("Utilizatorul nu are permisiunea de a crea taskuri.");
        }

        Task task = Task.builder()
                .orderId(dto.getOrderId())
                .teamId(dto.getTeamId())
                .userId(dto.getUserId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .taskType(dto.getTaskType())
                .status("NOT_STARTED")
                .scheduledDate(dto.getScheduledDate())
                .assignedBy(creatorUserId)
                .createdAt(LocalDateTime.now())
                .build();

        taskRepository.save(task);
        return mapper.map(task, TaskDto.class);
    }

    public TaskDto getById(UUID taskId, UUID companyId) {
        Task task = taskRepository.findByIdAndCompanyId(taskId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Task-ul nu a fost găsit"));
        return mapper.map(task, TaskDto.class);
    }

    public List<TaskDto> getAllForCompany(UUID companyId) {
        return taskRepository.findAllByCompanyId(companyId).stream()
                .map(task -> mapper.map(task, TaskDto.class))
                .collect(Collectors.toList());
    }

    public List<TaskDto> getAllForTeam(UUID teamId) {
        return taskRepository.findAllByTeamId(teamId).stream()
                .map(task -> mapper.map(task, TaskDto.class))
                .collect(Collectors.toList());
    }

    public List<TaskDto> getAllForUser(UUID userId) {
        return taskRepository.findAllByUserId(userId).stream()
                .map(task -> mapper.map(task, TaskDto.class))
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByDateAndCompany(LocalDate date, UUID companyId) {
        return taskRepository.findAllByScheduledDateAndCompanyId(date, companyId).stream()
                .map(task -> mapper.map(task, TaskDto.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateTaskStatus(UUID taskId, String newStatus, UUID companyId) {
        Task task = taskRepository.findByIdAndCompanyId(taskId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Task-ul nu a fost găsit în companie."));

        task.setStatus(newStatus);
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(UUID taskId, UUID requesterId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task-ul nu a fost găsit."));

        if (!task.getAssignedBy().equals(requesterId)) {
            throw new SecurityException("Doar utilizatorul care a creat taskul îl poate șterge.");
        }

        // 1. Găsește toate update-urile
        List<UUID> updateIds = taskUpdateRepository.findAllByTaskId(taskId).stream()
                .map(update -> update.getId())
                .toList();

        // 2. Șterge pozele asociate fiecărui update
        taskUpdatePhotoRepository.deleteAllByTaskUpdateIdIn(updateIds);

        // 3. Șterge update-urile
        taskUpdateRepository.deleteAllByTaskId(taskId);

        // 4. Șterge taskul propriu-zis
        taskRepository.delete(task);
    }


}
