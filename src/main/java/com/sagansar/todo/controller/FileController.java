package com.sagansar.todo.controller;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.work.TaskFile;
import com.sagansar.todo.service.FileService;
import com.sagansar.todo.service.SecurityService;
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

    @PostMapping("/upload")
    public void uploadFile(@RequestParam(name = "file") MultipartFile file, @RequestParam(name = "taskId") Long taskId) throws BadRequestException {
        if (!securityService.checkUserRights(RoleEnum.MANAGER)) {
            throw new BadRequestException("Недостаточно прав для загрузки файла!");
        }
        User user = securityService.getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Пользователь не найден!");
        }
        if (taskId == null) {
            throw new BadRequestException("Задача не найдена!");
        }
        fileService.storeFile(user, file, taskId);
    }

    @RequestMapping(path = "/download/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource> download(@PathVariable(name = "id") Long fileId) throws BadRequestException {
        TaskFile taskFile = fileService.getFile(fileId);
        ByteArrayResource resource = new ByteArrayResource(fileService.getFileContent(taskFile));

        return ResponseEntity.ok()
                .headers(headers(taskFile.getName()))
                .contentLength(taskFile.getSize())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @RequestMapping(path = "/video/{id}", method = RequestMethod.GET)
    public ResponseEntity<FileSystemResource> streamVideo(@PathVariable(name = "id") Long fileId) throws BadRequestException {
        TaskFile taskFile = fileService.getFile(fileId);
        return ResponseEntity.ok().body(new FileSystemResource(fileService.getFile(taskFile)));
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
