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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/database")
public class DatabaseController {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseController.class);
    private static final String DB_FILE_PATH = "database/budget_explorer_db";
    private static final String TEMP_DB_FILE_PATH = "database/budget_explorer_db.tmp";

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check endpoint called");
        return ResponseEntity.ok("Server is running");
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDatabase() {
        try {
            logger.info("Получен запрос на скачивание базы данных");
            File file = new File(DB_FILE_PATH);
            
            if (!file.exists()) {
                logger.error("Файл базы данных не найден: {}", DB_FILE_PATH);
                return ResponseEntity.notFound().build();
            }

            // Проверяем, что файл не пустой и имеет правильный размер
            if (file.length() == 0) {
                logger.error("Файл базы данных пустой");
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
            
            if (fileData.length == 0) {
                logger.error("Получены пустые данные");
                return ResponseEntity.badRequest().body("Получены пустые данные");
            }
            
            // Создаем директорию, если она не существует
            Path directory = Paths.get("database");
            if (!Files.exists(directory)) {
                logger.info("Создаем директорию для базы данных: {}", directory);
                Files.createDirectories(directory);
            }

            // Сначала сохраняем во временный файл
            Path tempFilePath = Paths.get(TEMP_DB_FILE_PATH);
            logger.info("Сохраняем во временный файл: {}", tempFilePath);
            
            try (FileOutputStream fos = new FileOutputStream(tempFilePath.toFile())) {
                fos.write(fileData);
                fos.flush();
            }
            
            // Проверяем временный файл
            File tempFile = tempFilePath.toFile();
            if (!tempFile.exists() || tempFile.length() != fileData.length) {
                logger.error("Ошибка при сохранении временного файла");
                return ResponseEntity.internalServerError().body("Ошибка при сохранении файла");
            }
            
            // Если все в порядке, перемещаем временный файл на место основного
            Path filePath = Paths.get(DB_FILE_PATH);
            logger.info("Перемещаем файл на место основного: {}", filePath);
            Files.move(tempFilePath, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Проверяем финальный файл
            File savedFile = filePath.toFile();
            if (savedFile.exists() && savedFile.length() == fileData.length) {
                logger.info("Файл базы данных успешно сохранен. Размер: {} байт", savedFile.length());
                return ResponseEntity.ok("База данных успешно загружена");
            } else {
                logger.error("Ошибка при сохранении файла базы данных");
                return ResponseEntity.internalServerError().body("Ошибка при сохранении файла");
            }
        } catch (IOException e) {
            logger.error("Ошибка при загрузке базы данных", e);
            return ResponseEntity.internalServerError().body("Ошибка при загрузке базы данных: " + e.getMessage());
        }
    }
} 