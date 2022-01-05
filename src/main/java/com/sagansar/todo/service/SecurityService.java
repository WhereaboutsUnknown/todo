package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.UnauthorizedException;
import com.sagansar.todo.infrastructure.validation.Validator;
import com.sagansar.todo.model.general.Contacts;
import com.sagansar.todo.model.general.Role;
import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.manager.Manager;
import com.sagansar.todo.model.work.Invite;
import com.sagansar.todo.model.work.InviteKey;
import com.sagansar.todo.model.worker.Worker;
import com.sagansar.todo.repository.*;
import lombok.AllArgsConstructor;
import org.passay.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;

import static org.passay.CharacterOccurrencesRule.ERROR_CODE;

@Service
@AllArgsConstructor
public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final ContactsRepository contactsRepository;
    private final RoleRepository roleRepository;
    private final ManagerRepository managerRepository;
    private final WorkerRepository workerRepository;
    private final InviteKeyRepository inviteKeyRepository;
    private final PasswordEncoder encoder;
    private final PasswordGenerator passwordGenerator;
    private final Validator passwordValidator;

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

    public String generateUrlInviteKey(@NonNull Invite invite) {
        String encodedKey = encoder.encode(invite.getId() + ":" + LocalDateTime.now(ZoneId.systemDefault()));
        InviteKey inviteKey = new InviteKey();
        inviteKey.setInvite(invite);
        inviteKey.setKey(encodedKey);

        return inviteKeyRepository.save(inviteKey).getKey();
    }

    public Invite authorizeInviteToken(String token) {
        InviteKey key = inviteKeyRepository.findDistinctByKey(token);
        if (key == null) {
            throw new UnauthorizedException("Токен не найден!");
        }
        Invite invite = key.getInvite();
        if (invite == null) {
            throw new UnauthorizedException("Приглашение не найдено!");
        }
        inviteKeyRepository.delete(key);
        return invite;
    }

    public String generatePassword() {
        return passwordGenerator.generatePassword(10, generationRules());
    }

    public boolean registerWorkerFromTelegram(String username, String password) {
        try {
            User user = registerUser(createUser(username, password, RoleEnum.FREELANCER));
            Contacts contacts = new Contacts();
            contacts.setTelegram(username);
            contacts.setUser(user);
            contactsRepository.save(contacts);
            registerWorker(user);
            return true;
        } catch (BadRequestException e) {
            logger.error(e.getResponseMessage());
            return false;
        }
    }

    public Worker registerWorker(@NonNull User user) {
        Worker worker = createWorker();
        worker.setUser(user);
        worker.setActive(true);
        return workerRepository.save(worker);
    }

    public Manager registerManager(@NonNull User user) {
        Manager manager = createManager();
        manager.setUser(user);
        manager.setActive(true);
        return managerRepository.save(manager);
    }

    private User registerUser(@NonNull User user) throws BadRequestException {
        user.setActive(true);
        return userRepository.save(user);
    }

    private User createUser(String username, String password, RoleEnum role) throws BadRequestException {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Пользователь с таким именем уже существует");
        }
        passwordValidator.validate(password);
        User user = new User();
        user.setUsername(username);
        String encodedPassword = encoder.encode(password);
        user.setPassword(encodedPassword);
        Role userRole = roleRepository.findByName(role.name());
        if (userRole == null) {
            throw new BadRequestException("Отсутствует роль пользователя");
        }
        user.setRoles(Set.of(userRole));
        if (RoleEnum.SUPERVISOR.equals(role)) {
            Role manager = roleRepository.findByName(RoleEnum.MANAGER.name());
            user.getRoles().add(manager);
        }
        return user;
    }

    private Worker createWorker() {
        return new Worker();
    }

    private Manager createManager() {
        return new Manager();
    }

    private CharacterRule[] generationRules() {
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(2);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(2);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ERROR_CODE;
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule splCharRule = new CharacterRule(specialChars);
        splCharRule.setNumberOfCharacters(2);

        return new CharacterRule[] { lowerCaseRule, upperCaseRule, digitRule, splCharRule };
    }
}
