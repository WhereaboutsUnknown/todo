package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.PersonNameDto;
import com.sagansar.todo.controller.mapper.PersonMapper;
import com.sagansar.todo.controller.util.RestResponse;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.UserBlockedException;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.WorkerRepository;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.ValidationService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProfileController {

    private final SecurityService securityService;

    private final ValidationService validationService;

    private final ManagerRepository managerRepository;

    private final WorkerRepository workerRepository;

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

    @GetMapping
    public PersonNameDto getCurrentUserProfile() {
        User user = securityService.getCurrentUser();
        if (user != null && user.isActive()) {
            if (securityService.checkUserRights(RoleEnum.MANAGER)) {
                Manager manager = managerRepository.findByUserId(user.getId())
                        .orElseThrow(() -> new UserBlockedException("Не найден профиль менеджера!"));
                if (!manager.isActive()) {
                    throw new UserBlockedException("Профиль менеджера не активен!");
                }
                return PersonMapper.managerToName(manager);
            }
            if (securityService.checkUserRights(RoleEnum.FREELANCER)) {
                Worker worker = workerRepository.findById(user.getId())
                        .orElseThrow(() -> new UserBlockedException("Не найден профиль исполнителя!"));
                if (!worker.isActive()) {
                    throw new UserBlockedException("Профиль исполнителя не активен!");
                }
                return PersonMapper.workerToName(worker);
            }
        }
        throw new UserBlockedException(user == null ? "Пользователь не найден!" : "Пользователь заблокирован!");
    }
}
