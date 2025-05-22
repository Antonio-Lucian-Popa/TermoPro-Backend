package com.asusoftware.TermoPro.task.repository;

import com.asusoftware.TermoPro.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findAllByCompanyId(UUID companyId);

    List<Task> findAllByTeamId(UUID teamId);

    List<Task> findAllByUserId(UUID userId);

    List<Task> findAllByScheduledDate(LocalDate scheduledDate);

    List<Task> findAllByScheduledDateAndCompanyId(LocalDate scheduledDate, UUID companyId);

    Optional<Task> findByIdAndCompanyId(UUID id, UUID companyId);

    boolean existsByIdAndCompanyId(UUID taskId, UUID companyId);
}
