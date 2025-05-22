package com.asusoftware.TermoPro.task.repository;

import com.asusoftware.TermoPro.task.model.TaskUpdatePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskUpdatePhotoRepository extends JpaRepository<TaskUpdatePhoto, UUID> {

    List<TaskUpdatePhoto> findAllByTaskUpdateId(UUID taskUpdateId);

    void deleteAllByTaskUpdateIdIn(List<UUID> updateIds);

}
