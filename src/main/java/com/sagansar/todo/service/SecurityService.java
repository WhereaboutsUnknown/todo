package com.sagansar.todo.service;

import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.ManagerRepository;
import com.sagansar.todo.repository.UserRepository;
import com.sagansar.todo.repository.WorkerRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
public class SecurityService {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final ManagerRepository managerRepository;
    private final WorkerRepository workerRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();

        UserDetails user;

        if (principal instanceof UserDetails) {
            user = (UserDetails) principal;
        } else {
            throw new RuntimeException("Unable to load user!");
        }
        return userRepository.findByUsername(user.getUsername());
    }

    public boolean checkUserRights(RoleEnum role) {
        Set<String> currentUserRoles = userDetailsService.getCurrentUserRoles();
        return (currentUserRoles.contains(role.name()) || currentUserRoles.contains("ADMIN"));
    }

    public boolean isAdmin() {
        Set<String> currentUserRoles = userDetailsService.getCurrentUserRoles();
        return currentUserRoles.contains("ADMIN");
    }

    public Manager getAuthorizedManager(Integer managerId) {
        if (!checkUserRights(RoleEnum.MANAGER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        Manager manager = managerRepository.findById(managerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Менеджер не найден"));
        User assignedUser = manager.getUser();
        User currentUser = getCurrentUser();
        if (!Objects.equals(assignedUser.getUsername(), currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        if (!manager.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Профиль менеджера был заблокирован");
        }
        return manager;
    }

    public Worker getAuthorizedWorker(Integer workerId) {
        if (!checkUserRights(RoleEnum.FREELANCER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        Worker worker = workerRepository.findById(workerId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Исполнитель не найден"));
        User assignedUser = worker.getUser();
        User currentUser = getCurrentUser();
        if (!Objects.equals(assignedUser.getUsername(), currentUser.getUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        if (!worker.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Профиль исполнителя был заблокирован");
        }
        return worker;
    }
}
