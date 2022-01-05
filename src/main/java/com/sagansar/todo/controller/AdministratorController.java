package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.UnitBasic;
import com.sagansar.todo.controller.dto.UserDto;
import com.sagansar.todo.controller.mapper.UserMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.UserRepository;
import com.sagansar.todo.repository.WorkerRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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

    @PostMapping("/workers")
    public UserDto createWorker() {
        return null;
    }

    @PostMapping("/managers")
    public UserDto createManager() {
        return null;
    }

    @PostMapping("/units")
    public UnitBasic createUnit() {
        return null;
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
