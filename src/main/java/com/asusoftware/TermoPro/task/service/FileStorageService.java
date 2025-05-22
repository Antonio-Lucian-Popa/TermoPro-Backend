package com.asusoftware.TermoPro.task.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {
    String saveFile(MultipartFile file, UUID taskUpdateId); // returnează URL sau path relativ
}
