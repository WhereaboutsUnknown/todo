package com.sagansar.todo.controller;

import com.sagansar.todo.controller.util.RestResponse;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    private final SecurityService securityService;

    private final ValidationService validationService;

    @PostMapping("/restore")
    public RestResponse restoreProfile() throws BadRequestException {
        securityService.restoreCurrentUserProfile();
        return new RestResponse("Профиль успешно восстановлен!");
    }

    @DeleteMapping("/manager/{id}")
    public RestResponse deleteManagerProfile(@PathVariable(name = "id") Integer id) throws BadRequestException {
        validationService.checkNullSafety(id);
        securityService.blockManagerProfile(id, true);
        return new RestResponse("Профиль удален!");
    }

    @DeleteMapping("/worker/{id}")
    public RestResponse deleteWorkerProfile(@PathVariable(name = "id") Integer id) throws BadRequestException {
        validationService.checkNullSafety(id);
        securityService.blockWorkerProfile(id, true);
        return new RestResponse("Профиль удален!");
    }
}
