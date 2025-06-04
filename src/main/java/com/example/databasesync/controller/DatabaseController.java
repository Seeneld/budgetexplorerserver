package com.example.databasesync.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    private static final String DB_FILE_PATH = "database/budget_explorer_db";

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDatabase() {
        try {
            logger.info("Получен запрос на скачивание базы данных");
            File file = new File(DB_FILE_PATH);
            
            if (!file.exists()) {
                logger.error("Файл базы данных не найден: {}", DB_FILE_PATH);
                return ResponseEntity.notFound().build();
            }

            logger.info("Размер файла базы данных: {} байт", file.length());
            Resource resource = new FileSystemResource(file);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Ошибка при скачивании базы данных", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDatabase(@RequestBody byte[] fileData) {
        try {
            logger.info("Получен запрос на загрузку базы данных. Размер данных: {} байт", fileData.length);
            
            // Создаем директорию, если она не существует
            Path directory = Paths.get("database");
            if (!Files.exists(directory)) {
                logger.info("Создаем директорию для базы данных: {}", directory);
                Files.createDirectories(directory);
            }

            // Сохраняем файл
            Path filePath = Paths.get(DB_FILE_PATH);
            logger.info("Сохраняем файл базы данных: {}", filePath);
            Files.write(filePath, fileData);
            
            // Проверяем, что файл действительно создался
            File savedFile = filePath.toFile();
            if (savedFile.exists()) {
                logger.info("Файл базы данных успешно сохранен. Размер: {} байт", savedFile.length());
            } else {
                logger.error("Файл не был создан после записи!");
            }

            return ResponseEntity.ok("База данных успешно загружена");
        } catch (IOException e) {
            logger.error("Ошибка при загрузке базы данных", e);
            return ResponseEntity.internalServerError().body("Ошибка при загрузке базы данных: " + e.getMessage());
        }
    }
} 