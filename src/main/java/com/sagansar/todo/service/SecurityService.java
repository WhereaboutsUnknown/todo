package com.sagansar.todo.service;

import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.infrastructure.exceptions.UnauthorizedException;
import com.sagansar.todo.infrastructure.exceptions.UserBlockedException;
import com.sagansar.todo.infrastructure.validation.Validator;
import com.sagansar.todo.model.external.RegistrationForm;
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
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        UserDetails user = getCurrentUserDetails();
        return userRepository.findByUsername(user.getUsername());
    }

    public boolean isManagerProfileDeleted() {
        UserDetails user = getCurrentUserDetails();
        return !managerRepository.existsByUserUsernameAndActiveTrue(user.getUsername());
    }

    public boolean isWorkerProfileDeleted() {
        UserDetails user = getCurrentUserDetails();
        return !workerRepository.existsByUserUsernameAndActiveTrue(user.getUsername());
    }

    public boolean isUserBlocked() {
        UserDetails user = getCurrentUserDetails();
        return !userRepository.existsByUsernameAndActiveTrue(user.getUsername());
    }

    public boolean checkUserRights(RoleEnum role) {
        Set<String> currentUserRoles = userDetailsService.getCurrentUserRoles();
        return (currentUserRoles.contains(role.name()) || currentUserRoles.contains("ADMIN"));
    }

    public boolean checkUserRights(User user, RoleEnum role) {
        return user.getRoles().stream().anyMatch(userRole -> RoleEnum.fromId(userRole.getId()).equals(role));
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

    public Worker getAuthorizedWorker() {
        if (!checkUserRights(RoleEnum.FREELANCER)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Доступ запрещен");
        }
        User user = getCurrentUser();
        if (user == null) {
            throw new UnauthorizedException("Доступ запрещен");
        }
        return workerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedException("Доступ запрещен"));
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

    public void destroyInviteTokens(@NonNull Long inviteId) {
        inviteKeyRepository.deleteAllByInviteId(inviteId);
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
        Worker worker = workerRepository.findByUserId(user.getId()).orElse(createWorker());
        worker.setUser(user);
        worker.setActive(true);
        return workerRepository.save(worker);
    }

    public Manager registerManager(@NonNull User user) {
        Manager manager = managerRepository.findByUserId(user.getId()).orElse(createManager());
        manager.setUser(user);
        manager.setActive(true);
        return managerRepository.save(manager);
    }

    public User registerUser(RegistrationForm registrationForm, RoleEnum role) throws BadRequestException {
        if (registrationForm == null) {
            throw new BadRequestException("Отсутствует форма регистрации");
        }
        String username = registrationForm.getUsername();
        String password = registrationForm.getPassword();
        checkUsername(username);
        passwordValidator.validate(password);
        User user = registerUser(createUser(username, password, role));
        if (RoleEnum.MANAGER.equals(role)) {
            registerManager(user);
        } else if (RoleEnum.FREELANCER.equals(role)) {
            registerWorker(user);
        }
        return user;
    }

    public void checkUsername(String username) throws BadRequestException {
        if (!StringUtils.hasText(username)) {
            throw new BadRequestException("Имя пользователя не может быть пустым!");
        }
        if (!username.matches("$[A-Za-z0-9]+^")) {
            throw new BadRequestException("Имя пользователя должно содержать только цифры и латинские буквы!");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Пользователь с таким именем уже зарегистрирован!");
        }
    }

    public User setSupervisor(@NonNull User user, boolean grantSupervisor) throws BadRequestException {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        if (!roles.contains(RoleEnum.MANAGER.name())) {
            throw new BadRequestException("У пользователя отсутствует роль менеджера!");
        }
        boolean isSupervisor = roles.contains(RoleEnum.SUPERVISOR.name());
        if (grantSupervisor && !isSupervisor) {
            user.setRoles(user.getRoles().stream()
                    .filter(role -> !RoleEnum.SUPERVISOR.name().equals(role.getName()))
                    .collect(Collectors.toSet()));
            return userRepository.save(user);
        }
        if (!grantSupervisor && isSupervisor) {
            Role supervisor = roleRepository.findByName(RoleEnum.SUPERVISOR.name());
            user.getRoles().add(supervisor);
            return userRepository.save(user);
        }
        return user;
    }

    public UserDetails getCurrentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        Object principal = auth.getPrincipal();

        if (principal instanceof UserDetails) {
            return  (UserDetails) principal;
        } else {
            throw new RuntimeException("Unable to load user!");
        }
    }

    public void restoreCurrentUserProfile() throws BadRequestException {
        User user = getCurrentUser();
        if (user == null) {
            throw new BadRequestException("Пользователь не найден!");
        }
        if (!user.isActive()) {
            throw new UserBlockedException("Невозможно восстановить профиль: пользователь заблокирован");
        }
        if (checkUserRights(RoleEnum.FREELANCER)) {
            Worker worker = workerRepository.findByUserId(user.getId())
                    .orElse(registerWorker(user));
            if (!worker.isActive()) {
                setWorkerProfileBlock(worker, false);
            }
        }
        if (checkUserRights(RoleEnum.MANAGER)) {
            Manager manager = managerRepository.findByUserId(user.getId())
                    .orElse(registerManager(user));
            if (!manager.isActive()) {
                setManagerProfileBlock(manager, false);
            }
        }
    }

    public Manager blockManagerProfile(Integer managerId, boolean block) throws BadRequestException {
        Manager manager = managerRepository.findById(managerId)
                .orElseThrow(() -> new BadRequestException("Профиль менеджера не найден"));
        return setManagerProfileBlock(manager, block);
    }

    public Worker blockWorkerProfile(Integer workerId, boolean block) throws BadRequestException {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new BadRequestException("Профиль исполнителя не найден"));
        return setWorkerProfileBlock(worker, block);
    }

    private Manager setManagerProfileBlock(Manager manager, boolean block) {
        if ((manager.isActive() && !block) || (!manager.isActive() && block)) {
            return manager;
        }
        manager.setActive(!block);
        return managerRepository.save(manager);
    }

    private Worker setWorkerProfileBlock(Worker worker, boolean block) {
        if ((worker.isActive() && !block) || (!worker.isActive() && block)) {
            return worker;
        }
        worker.setActive(!block);
        return workerRepository.save(worker);
    }

    public void addUserAvatar(User user, Long avatarId) {
        user.setAvatar(avatarId);
        userRepository.save(user);
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
