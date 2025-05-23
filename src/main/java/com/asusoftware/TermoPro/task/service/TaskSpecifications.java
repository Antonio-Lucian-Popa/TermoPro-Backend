package com.asusoftware.TermoPro.task.service;

import com.asusoftware.TermoPro.task.model.Task;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class TaskSpecifications {

    public static Specification<Task> hasCompanyId(UUID companyId) {
        return (root, query, cb) -> cb.equal(root.get("companyId"), companyId);
    }

    public static Specification<Task> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasType(String type) {
        return (root, query, cb) -> cb.equal(root.get("taskType"), type);
    }
}