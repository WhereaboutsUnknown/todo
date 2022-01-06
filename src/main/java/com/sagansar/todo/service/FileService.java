package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.TaskFile;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.FileRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;

    private final String fileStoragePath;

    private final TodoTaskRepository todoTaskRepository;

    public FileService(FileRepository fileRepository,
                       TodoTaskRepository todoTaskRepository,
                       @Value("${file.storage}") String fileStorage) {
        this.fileRepository = fileRepository;
        this.todoTaskRepository = todoTaskRepository;
        this.fileStoragePath = fileStorage;
    }

    public void storeFile(@NonNull User creator, MultipartFile multipartFile, @NonNull Long taskId) throws BadRequestException {
        if (!multipartFile.isEmpty()) {
            try {
                TodoTask task = todoTaskRepository.findById(taskId)
                        .orElseThrow(() -> new BadRequestException("Задача не найдена!"));
                LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
                String originalName = multipartFile.getOriginalFilename();
                File file = Paths.get(fileStoragePath, (StringUtils.hasText(originalName) ? originalName : now + ".txt")).toFile();
                multipartFile.transferTo(file);
                TaskFile taskFile = createFile(file.getName(), multipartFile.getSize());
                taskFile.setTask(task);
                taskFile.setCreator(creator);
                taskFile.setUploadDate(now);
                fileRepository.save(taskFile);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.error("Загруженный файл отсутствует!");
        }
    }

    public TaskFile getFile(Long id) throws BadRequestException {
        if (id == null) {
            throw new BadRequestException("Отсутствует идентификатор файла!");
        }
        return fileRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Файл не найден!"));
    }

    public byte[] getFileContent(TaskFile taskFile) throws BadRequestException {
        Path path = Paths.get(fileStoragePath, taskFile.getName());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Ошибка при чтении файла!");
        }
    }

    public File getFile(TaskFile taskFile) {
        return Paths.get(fileStoragePath, taskFile.getName()).toFile();
    }

    private TaskFile createFile(String name, long size) {
        TaskFile taskFile = new TaskFile();
        taskFile.setName(name);
        taskFile.setSize(size);
        taskFile.setDeleted(false);
        taskFile.setInUse(true);
        return taskFile;
    }

}
