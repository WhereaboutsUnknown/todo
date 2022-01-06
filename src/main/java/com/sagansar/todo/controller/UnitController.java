package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.UnitFull;
import com.sagansar.todo.controller.mapper.UnitMapper;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.manager.Unit;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.UnitRepository;
import com.sagansar.todo.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@PreAuthorize("hasAuthority('SUPERVISOR')")
@RestController
@Transactional
@AllArgsConstructor
@ResponseBody
@RequestMapping(path = "/unit/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
public class UnitController {

    private final UnitRepository unitRepository;

    private final ManagerRepository managerRepository;

    private final SecurityService securityService;

    @GetMapping("")
    public UnitFull getUnit(@PathVariable Integer id) throws BadRequestException {
        Unit unit = findUnit(id);
        return UnitMapper.unitToFull(unit);
    }

    @PutMapping("")
    public UnitFull addManager(@PathVariable Integer id, @RequestBody Integer managerId) throws BadRequestException {
        if (managerId == null) {
            throw new BadRequestException("Отсутствует идентификатор менеджера!");
        }
        Unit unit = findUnit(id);
        if (!contains(unit, managerId)) {
            Manager manager = managerRepository.findById(managerId)
                    .orElseThrow(() -> new BadRequestException("Менеджер не найден!"));
            manager.setUnit(unit);
            unit.getManagers().add(manager);
        }
        return UnitMapper.unitToFull(unit);
    }

    @DeleteMapping("")
    public UnitFull removeManager(@PathVariable Integer id, @RequestBody Integer managerId) throws BadRequestException {
        if (managerId == null) {
            throw new BadRequestException("Отсутствует идентификатор менеджера!");
        }
        Unit unit = findUnit(id);
        if (contains(unit, managerId)) {
            Manager manager = managerRepository.findById(managerId)
                    .orElseThrow(() -> new BadRequestException("Менеджер не найден!"));
            manager.setUnit(null);
            unit.getManagers().remove(manager);
        }
        return UnitMapper.unitToFull(unit);
    }

    public Unit findUnit(Integer id) throws BadRequestException {
        if (id == null) {
            throw new BadRequestException("Отсутствует идентификатор отдела!");
        }
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Отдел не найден!"));
        Set<Integer> managers = unit.getManagers().stream()
                .map(Manager::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toSet());
        User currentUser = securityService.getCurrentUser();
        if (currentUser == null || !managers.contains(currentUser.getId())) {
            throw new BadRequestException("Пользователь не относится к отделу!");
        }
        return unit;
    }

    public boolean contains(Unit unit, Integer managerId) {
        return unit.getManagers().stream()
                .map(Manager::getId)
                .collect(Collectors.toSet())
                .contains(managerId);
    }
}
