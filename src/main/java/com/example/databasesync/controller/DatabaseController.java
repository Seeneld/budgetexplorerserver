package com.example.databasesync.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/database")
public class DatabaseController {

    private static final String DB_FILE_PATH = "database/budget_explorer.db";

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDatabase() {
        try {
            File file = new File(DB_FILE_PATH);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDatabase(@RequestParam("file") MultipartFile file) {
        try {
            // Создаем директорию, если она не существует
            Path directory = Paths.get("database");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // Сохраняем файл
            Path filePath = Paths.get(DB_FILE_PATH);
            Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok("База данных успешно загружена");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Ошибка при загрузке базы данных: " + e.getMessage());
        }
    }
} 