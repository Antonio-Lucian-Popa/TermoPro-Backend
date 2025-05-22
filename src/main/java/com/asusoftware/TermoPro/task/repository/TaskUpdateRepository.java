package com.asusoftware.TermoPro.task.repository;

import com.asusoftware.TermoPro.task.model.TaskUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskUpdateRepository extends JpaRepository<TaskUpdate, UUID> {

    List<TaskUpdate> findAllByTaskId(UUID taskId);

    List<TaskUpdate> findAllByUserId(UUID userId);

    List<TaskUpdate> findAllByTaskIdOrderByTimestampAsc(UUID taskId);

    boolean existsByTaskId(UUID taskId);

    void deleteAllByTaskId(UUID taskId);

}
