package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.*;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.TaskFile;
import com.sagansar.todo.model.work.TodoStatus;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.StoredFileRepository;
import com.sagansar.todo.repository.TaskFileRepository;
import com.sagansar.todo.repository.TodoTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    private final TaskFileRepository taskFileRepository;

    private final StoredFileRepository storedFileRepository;

    private final SecurityService securityService;

    private final String taskFileStoragePath;

    private final String usersFileStoragePath;

    private final String appUserFilesStoragePath;

    private final TodoTaskRepository todoTaskRepository;

    public FileService(TaskFileRepository taskFileRepository,
                       TodoTaskRepository todoTaskRepository,
                       StoredFileRepository storedFileRepository,
                       SecurityService securityService,
                       @Value("${file.task.storage}") String taskFileStoragePath,
                       @Value("${file.user.storage}") String usersFileStoragePath,
                       @Value("${file.app.user.storage}") String appUserFilesStoragePath) {
        this.taskFileRepository = taskFileRepository;
        this.storedFileRepository = storedFileRepository;
        this.todoTaskRepository = todoTaskRepository;
        this.securityService = securityService;
        this.taskFileStoragePath = taskFileStoragePath;
        this.usersFileStoragePath = usersFileStoragePath;
        this.appUserFilesStoragePath = appUserFilesStoragePath;
    }

    public void storeFile(@NonNull User creator, MultipartFile multipartFile, @NonNull Long taskId) throws BadRequestException {
        if (!multipartFile.isEmpty()) {
            try {
                TodoTask task = todoTaskRepository.findById(taskId)
                        .orElseThrow(() -> new BadRequestException("???????????? ???? ??????????????!"));
                LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
                String originalName = multipartFile.getOriginalFilename();
                File file = Paths.get(taskFileStoragePath, (StringUtils.hasText(originalName) ? originalName : now + ".txt")).toFile();
                multipartFile.transferTo(file);
                TaskFile taskFile = createFile(file.getName(), multipartFile.getSize());
                taskFile.setTask(task);
                taskFile.setCreator(creator);
                taskFile.setUploadDate(now);
                taskFileRepository.save(taskFile);
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        } else {
            logger.error("?????????????????????? ???????? ??????????????????????!");
        }
    }

    public Long storeAvatar(@NonNull User user, MultipartFile multipartFile) throws BadRequestException {
        if (!multipartFile.isEmpty()) {
            try {
                String fileFormat = resolveImageFormat(multipartFile.getOriginalFilename());
                String fileName = user.getUsername() + "_" + "avatar" + fileFormat;
                File file = Paths.get(usersFileStoragePath, fileName).toFile();
                multipartFile.transferTo(file);
                StoredFile storedFile = saveFile(user, file);
                return storedFile.getId();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        logger.error("?????????????????????? ???????? ??????????????????????!");
        throw new BadRequestException("?????????????????????? ???????? ??????????????????????!");
    }

    public TaskFile getTaskFile(Long id, @NonNull User user) throws BadRequestException {
        if (id == null) {
            throw new BadRequestException("?????????????????????? ?????????????????????????? ??????????!");
        }
        TaskFile file = taskFileRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("???????? ???? ????????????!", HttpStatus.NOT_FOUND));
        checkUserAccessToTaskFile(file, user);
        return file;
    }

    public byte[] getFileContent(TaskFile taskFile) throws BadRequestException {
        Path path = Paths.get(taskFileStoragePath, taskFile.getName());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("???????????? ?????? ???????????? ??????????!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public File getUserAvatar(Long avatarId) {
        try {
            return getUserFile(avatarId);
        } catch (BadRequestException e) {
            Path path = Paths.get(appUserFilesStoragePath, FilePurpose.USER_AVATAR.getDefaultFile());
            return path.toFile();
        }
    }

    public File getUserFile(Long fileId) throws BadRequestException {
        if (fileId == null) {
            throw new BadRequestException("?????????????????????? ?????????????????????????? ??????????!");
        }
        StoredFile storedFile = storedFileRepository.findById(fileId)
                .orElseThrow(() -> new BadRequestException("???????? ???? ????????????!", HttpStatus.NOT_FOUND));
        Path path = Paths.get(usersFileStoragePath, storedFile.getName());
        return path.toFile();
    }

    public File getUserFile(Long fileId, Long purpose) throws BadRequestException {
        FilePurpose filePurpose = FilePurpose.fromId(purpose);
        if (storedFileRepository.existsById(fileId)) {
            return getUserFile(fileId);
        }
        Path path = Paths.get(appUserFilesStoragePath, filePurpose.getDefaultFile());
        return path.toFile();
    }

    public File getFile(TaskFile taskFile) {
        return Paths.get(taskFileStoragePath, taskFile.getName()).toFile();
    }

    public void deleteTaskFiles(TodoTask task) {
        List<TaskFile> filesToDelete = new ArrayList<>();
        for (TaskFile taskFile : task.getFiles()) {
            File file = getFile(taskFile);
            if (file.exists() && file.delete()) {
                filesToDelete.add(taskFile);
            }
        }
        taskFileRepository.deleteAll(filesToDelete);
    }

    private TaskFile createFile(String name, long size) {
        TaskFile taskFile = new TaskFile();
        taskFile.setName(name);
        taskFile.setSize(size);
        taskFile.setDeleted(false);
        taskFile.setInUse(true);
        return taskFile;
    }

    private StoredFile saveFile(User user, File file) {
        StoredFile storedFile = new StoredFile();
        storedFile.setName(file.getName());
        storedFile.setDeleted(false);
        storedFile.setInUse(true);
        storedFile.setSize(file.length());
        storedFile.setCreator(user);
        storedFile.setUploadDate(LocalDateTime.now(ZoneId.systemDefault()));
        return storedFileRepository.save(storedFile);
    }

    private String resolveImageFormat(String fileName) throws BadRequestException {
        Extension extension = Extension.resolve(fileName);
        if (!extension.isImage()) {
            throw new BadRequestException("???????? ???? ?????????????????????????? ???????????????????? ??????????????!");
        }
        return "." + extension.name().toLowerCase();
    }

    private void checkUserAccessToTaskFile(TaskFile file, User user) throws BadRequestException {
        TodoTask task = file.getTask();
        if (securityService.checkUserRights(user, RoleEnum.FREELANCER) && (task.is(TodoStatus.Status.TODO) || task.is(TodoStatus.Status.DISCUSSION))) {
            return;
        }
        Set<Integer> fileRelatedUsers = new HashSet<>();
        fileRelatedUsers.add(file.getCreator().getId());
        fileRelatedUsers.addAll(
                task.getUnit().getManagers().stream()
                        .map(Manager::getUser)
                        .filter(User::isActive)
                        .map(User::getId)
                        .collect(Collectors.toSet())
        );
        fileRelatedUsers.addAll(
                task.getGroup().stream()
                        .map(workerGroupTask -> workerGroupTask.getWorker().getUser())
                        .filter(User::isActive)
                        .map(User::getId)
                        .collect(Collectors.toSet())
        );
        if (!fileRelatedUsers.contains(user.getId())) {
            throw new BadRequestException("???????????????????????? ???????? ?????? ?????????????? ?? ??????????", HttpStatus.FORBIDDEN);
        }
    }

}
