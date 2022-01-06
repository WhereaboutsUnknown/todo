package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.UnitBasic;
import com.sagansar.todo.controller.dto.UserDto;
import com.sagansar.todo.controller.mapper.UnitMapper;
import com.sagansar.todo.controller.mapper.UserMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.external.RegistrationForm;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.UnitRepository;
import com.sagansar.todo.repository.UserRepository;
import com.sagansar.todo.repository.WorkerRepository;
import com.sagansar.todo.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/admin", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdministratorController {

    private final UserRepository userRepository;

    private final ManagerRepository managerRepository;

    private final WorkerRepository workerRepository;

    private final UnitRepository unitRepository;

    private final SecurityService securityService;

    @PostMapping("/workers")
    public UserDto createWorker(@RequestBody RegistrationForm registrationForm) throws BadRequestException {
        return UserMapper.userToDto(securityService.registerUser(registrationForm, RoleEnum.FREELANCER));
    }

    @PostMapping("/managers")
    public UserDto createManager(@RequestBody RegistrationForm registrationForm) throws BadRequestException {
        return UserMapper.userToDto(securityService.registerUser(registrationForm, RoleEnum.MANAGER));
    }

    @PostMapping("/units")
    public UnitBasic createUnit(@RequestBody String name, @RequestBody Integer userId) throws BadRequestException {
        if (!StringUtils.hasText(name)) {
            throw new BadRequestException("Наименование отдела не может быть пустым!");
        }
        Manager manager = managerRepository.findByUserId(userId);
        if (manager == null) {
            throw new BadRequestException("Менеджер не найден!");
        }
        Unit unit = new Unit();
        unit.setName(name);
        unit.setManagers(List.of(manager));
        return UnitMapper.unitToBasic(unitRepository.save(unit));
    }

    @PutMapping("/managers/{id}/supervisor")
    public UserDto setSupervisor(@PathVariable Integer id, @RequestParam boolean isSupervisor) throws BadRequestException {
        User user = userRepository.findById(id).orElseThrow(() -> new BadRequestException("Пользователь не найден!"));
        return UserMapper.userToDto(securityService.setSupervisor(user, isSupervisor));
    }

    @PutMapping("/managers/{id}")
    public boolean changeManagerStatus(@PathVariable Integer id, @RequestParam boolean block) throws BadRequestException {
        return setUserStatus(id, block);
    }

    @PutMapping("/workers/{id}")
    public boolean changeWorkerStatus(@PathVariable Integer id, @RequestParam boolean block) throws BadRequestException {
        return setUserStatus(id, block);
    }

    @GetMapping("/managers")
    public List<UserDto> getManagers(Pageable pageable) {
        return managerRepository.findAll(pageable).stream()
                .map(Manager::getUser)
                .map(UserMapper::userToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/workers")
    public List<UserDto> getWorkers(Pageable pageable) {
        return workerRepository.findAll(pageable).stream()
                .map(Worker::getUser)
                .map(UserMapper::userToDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/units")
    public List<UnitBasic> getUnits(Pageable pageable) {
        return unitRepository.findAll(pageable).stream()
                .map(UnitMapper::unitToBasic)
                .collect(Collectors.toList());
    }

    private boolean setUserStatus(Integer id, boolean block) throws BadRequestException {
        if (id == null) {
            throw new BadRequestException("Требуется ID пользователя");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Пользователь не найден!"));
        if (block) {
            user.block();
        } else {
            user.unblock();
        }
        return user.isActive();
    }
}
