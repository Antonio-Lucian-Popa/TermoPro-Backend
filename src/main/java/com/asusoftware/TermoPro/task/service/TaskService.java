package com.asusoftware.TermoPro.task.service;

import com.asusoftware.TermoPro.customer_order.model.CustomerOrder;
import com.asusoftware.TermoPro.customer_order.model.OrderStatus;
import com.asusoftware.TermoPro.customer_order.repository.CustomerOrderRepository;
import com.asusoftware.TermoPro.exception.ResourceNotFoundException;
import com.asusoftware.TermoPro.task.model.Task;
import com.asusoftware.TermoPro.task.model.TaskUpdate;
import com.asusoftware.TermoPro.task.model.dto.CreateTaskDto;
import com.asusoftware.TermoPro.task.model.dto.DashboardStatsDto;
import com.asusoftware.TermoPro.task.model.dto.TaskDto;
import com.asusoftware.TermoPro.task.repository.TaskRepository;
import com.asusoftware.TermoPro.task.repository.TaskUpdatePhotoRepository;
import com.asusoftware.TermoPro.task.repository.TaskUpdateRepository;
import com.asusoftware.TermoPro.team.model.Team;
import com.asusoftware.TermoPro.team.model.TeamMember;
import com.asusoftware.TermoPro.team.repository.TeamMembersRepository;
import com.asusoftware.TermoPro.team.repository.TeamRepository;
import com.asusoftware.TermoPro.user.model.User;
import com.asusoftware.TermoPro.user.model.UserRole;
import com.asusoftware.TermoPro.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
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
    private final CustomerOrderRepository orderRepository;
    private final TeamRepository teamRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final ModelMapper mapper;

    @Transactional
    public TaskDto createTask(CreateTaskDto dto, UUID creatorUserId) {
        if (dto.getTeamId() == null && dto.getUserId() == null) {
            throw new IllegalArgumentException("Trebuie să specifici fie teamId, fie userId.");
        }

        // Verifica daca user are rol de admin in companie
        User userCreator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizatorul nu a fost găsit"));

        if (!userCreator.getRole().equals(UserRole.OWNER) && !userCreator.getRole().equals(UserRole.MANAGER)) {
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
                .companyId(dto.getCompanyId())
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

    public Page<TaskDto> getAllForCompany(UUID companyId, String status, String type, Pageable pageable) {
        Specification<Task> spec = Specification.where(TaskSpecifications.hasCompanyId(companyId));

        if (status != null && !status.equals("all")) {
            spec = spec.and(TaskSpecifications.hasStatus(status));
        }

        if (type != null && !type.equals("all")) {
            spec = spec.and(TaskSpecifications.hasType(type));
        }

        return taskRepository.findAll(spec, pageable)
                .map(task -> mapper.map(task, TaskDto.class));
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

    public DashboardStatsDto getDashboardStats(UUID companyId) {
        List<Task> allTasks = taskRepository.findAllByCompanyId(companyId);
        List<CustomerOrder> allOrders = orderRepository.findAllByCompanyId(companyId);
        LocalDate today = LocalDate.now();

        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setTotalTasks(allTasks.size());
        dto.setPendingTasks((int) allTasks.stream().filter(t -> t.getStatus().equals("PENDING")).count());
        dto.setCompletedTasks((int) allTasks.stream().filter(t -> t.getStatus().equals("COMPLETED")).count());

        List<Team> allTeams = teamRepository.findAllByCompanyId(companyId);
        List<TeamMember> allMembers = teamMembersRepository.findAllByTeamIdIn(
                allTeams.stream().map(Team::getId).toList()
        );

        dto.setTotalTeams(allTeams.size());
        dto.setTotalTeamMembers(allMembers.size());


        dto.setRecentTasks(
                allTasks.stream()
                        .sorted(Comparator.comparing(Task::getCreatedAt).reversed())
                        .limit(5)
                        .map(t -> mapper.map(t, TaskDto.class))
                        .toList()
        );

        dto.setTodayTasks(
                allTasks.stream()
                        .filter(t -> today.equals(t.getScheduledDate()))
                        .map(t -> mapper.map(t, TaskDto.class))
                        .toList()
        );

        dto.setTotalOrders(allOrders.size());
        dto.setPendingOrders((int) allOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.PENDING)).count());
        dto.setCompletedOrders((int) allOrders.stream().filter(o -> o.getStatus().equals(OrderStatus.COMPLETED)).count());

        dto.setUpcomingTimeOff(Collections.emptyList()); // sau folosește date reale dacă ai

        return dto;
    }



    @Transactional
    public TaskDto updateTaskStatus(UUID taskId, String newStatus, UUID companyId) {
        Task task = taskRepository.findByIdAndCompanyId(taskId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Task-ul nu a fost găsit în companie."));

        task.setStatus(newStatus);
        taskRepository.save(task);
        return mapper.map(task, TaskDto.class);
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

    public byte[] exportTasksWithUpdatesToPdf(UUID teamId, LocalDate date) {
        List<Task> tasks = (date != null)
                ? taskRepository.findAllByTeamIdAndScheduledDate(teamId, date)
                : taskRepository.findAllByTeamId(teamId);

        String title = "Raport taskuri echipă: " + teamId + (date != null ? " | Data: " + date : "");
        return generateTaskPdf(tasks, title);
    }

    public byte[] exportUserTasksWithUpdatesToPdf(UUID userId, LocalDate date) {
        List<Task> tasks = (date != null)
                ? taskRepository.findAllByUserIdAndScheduledDate(userId, date)
                : taskRepository.findAllByUserId(userId);

        String title = "Raport taskuri user: " + userId + (date != null ? " | Data: " + date : "");
        return generateTaskPdf(tasks, title);
    }

    public byte[] exportTasksWithUpdatesToExcel(UUID teamId, LocalDate date) {
        List<Task> tasks = (date != null)
                ? taskRepository.findAllByTeamIdAndScheduledDate(teamId, date)
                : taskRepository.findAllByTeamId(teamId);
        return generateTaskExcel(tasks, "Taskuri_Echipa");
    }

    public byte[] exportUserTasksWithUpdatesToExcel(UUID userId, LocalDate date) {
        List<Task> tasks = (date != null)
                ? taskRepository.findAllByUserIdAndScheduledDate(userId, date)
                : taskRepository.findAllByUserId(userId);
        return generateTaskExcel(tasks, "Taskuri_User");
    }


    private byte[] generateTaskPdf(List<Task> tasks, String title) {
        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("Nu există taskuri pentru filtrarea aleasă.");
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream content = new PDPageContentStream(doc, page);
            float yStart = 780;
            float y = yStart;
            float margin = 50;
            float leading = 18f;

            content.setFont(PDType1Font.HELVETICA_BOLD, 14);
            content.beginText();
            content.newLineAtOffset(margin, y);
            content.showText(title);
            content.endText();
            y -= 30;

            for (Task task : tasks) {
                if (y < 120) {
                    content.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    content = new PDPageContentStream(doc, page);
                    y = yStart;
                }

                content.setFont(PDType1Font.HELVETICA_BOLD, 12);
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText("Task: " + task.getTitle() + " [" + task.getTaskType() + "]");
                content.endText();
                y -= leading;

                content.setFont(PDType1Font.HELVETICA, 11);
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText("Data programare: " + task.getScheduledDate() + " | Status: " + task.getStatus());
                content.endText();
                y -= leading;

                content.moveTo(margin, y);
                content.lineTo(page.getMediaBox().getWidth() - margin, y);
                content.stroke();
                y -= 10;

                List<TaskUpdate> updates = taskUpdateRepository.findAllByTaskIdOrderByTimestampAsc(task.getId());
                for (TaskUpdate update : updates) {
                    if (y < 100) {
                        content.close();
                        page = new PDPage(PDRectangle.A4);
                        doc.addPage(page);
                        content = new PDPageContentStream(doc, page);
                        y = yStart;
                    }

                    content.beginText();
                    content.setFont(PDType1Font.HELVETICA, 10);
                    content.newLineAtOffset(margin + 20, y);
                    content.showText("- [" + update.getTimestamp().toLocalDate() + "] "
                            + update.getStatus()
                            + " (by userId: " + update.getUserId() + ")"
                            + (update.getComment() != null ? " - " + update.getComment() : ""));
                    content.endText();
                    y -= leading;
                }

                y -= 20;
            }

            content.close();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Eroare la generarea PDF-ului", e);
        }
    }

    private byte[] generateTaskExcel(List<Task> tasks, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            int rowIdx = 0;

            // Header
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Titlu");
            header.createCell(1).setCellValue("Tip");
            header.createCell(2).setCellValue("Dată programare");
            header.createCell(3).setCellValue("Status");
            header.createCell(4).setCellValue("Comentarii");
            header.createCell(5).setCellValue("User Update");
            header.createCell(6).setCellValue("Dată Update");

            for (Task task : tasks) {
                List<TaskUpdate> updates = taskUpdateRepository.findAllByTaskIdOrderByTimestampAsc(task.getId());

                if (updates.isEmpty()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(task.getTitle());
                    row.createCell(1).setCellValue(task.getTaskType());
                    row.createCell(2).setCellValue(task.getScheduledDate().toString());
                    row.createCell(3).setCellValue(task.getStatus());
                } else {
                    for (TaskUpdate update : updates) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(task.getTitle());
                        row.createCell(1).setCellValue(task.getTaskType());
                        row.createCell(2).setCellValue(task.getScheduledDate().toString());
                        row.createCell(3).setCellValue(task.getStatus());
                        row.createCell(4).setCellValue(update.getComment() != null ? update.getComment() : "");
                        row.createCell(5).setCellValue(update.getUserId().toString());
                        row.createCell(6).setCellValue(update.getTimestamp().toString());
                    }
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Eroare la generarea Excel-ului", e);
        }
    }


}
