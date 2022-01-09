package com.sagansar.todo.controller;

import com.sagansar.todo.controller.util.RestResponse;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.TaskFile;
import com.sagansar.todo.service.FileService;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.TodoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Transactional
@AllArgsConstructor
@RequestMapping(path = "/file-service")
public class FileController {

    private final SecurityService securityService;

    private final FileService fileService;

    private final TodoService todoService;

    @PostMapping("/upload")
    public RestResponse uploadFile(@RequestParam(name = "file") MultipartFile file, @RequestParam(name = "taskId") Long taskId) throws BadRequestException {
        User user = securityService.getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Пользователь не найден!");
        }
        if (taskId == null) {
            throw new BadRequestException("Задача не найдена!");
        }
        todoService.checkUserRightsOnTaskAsManager(user.getId(), taskId);
        fileService.storeFile(user, file, taskId);
        return new RestResponse("Файл загружен!");
    }

    @PostMapping("/upload/avatar")
    public RestResponse uploadAvatar(@RequestParam(name = "file") MultipartFile file) throws BadRequestException {
        User user = securityService.getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Пользователь не найден!");
        }
        Long avatarId = fileService.storeAvatar(user, file);
        securityService.addUserAvatar(user, avatarId);
        return new RestResponse("Фото профиля обновлено!");
    }

    @RequestMapping(path = "/download/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@PathVariable(name = "id") Long fileId) throws BadRequestException {
        User user = securityService.getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Пользователь не найден!");
        }
        TaskFile taskFile = fileService.getTaskFile(fileId, user);
        ByteArrayResource resource = new ByteArrayResource(fileService.getFileContent(taskFile));

        return ResponseEntity.ok()
                .headers(headers(taskFile.getName()))
                .contentLength(taskFile.getSize())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @RequestMapping(path = "/video/{id}", method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> streamVideo(@PathVariable(name = "id") Long fileId) throws BadRequestException {
        User user = securityService.getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Пользователь не найден!");
        }
        TaskFile taskFile = fileService.getTaskFile(fileId, user);
        return ResponseEntity.ok().body(new FileSystemResource(fileService.getFile(taskFile)));
    }

    @RequestMapping(path = "/user/file/{id}", method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> getUserFile(@PathVariable(name = "id") Long fileId) throws BadRequestException {
        return ResponseEntity.ok().body(new FileSystemResource(fileService.getUserFile(fileId)));
    }

    @RequestMapping(path = "/user/avatar", method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> getUserAvatar() throws BadRequestException {
        User user = securityService.getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Пользователь не найден!");
        }
        return ResponseEntity.ok().body(new FileSystemResource(fileService.getUserAvatar(user.getAvatar())));
    }

    private HttpHeaders headers(String fileName) {
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");
        return header;
    }
}
