package com.sagansar.todo.controller;

import com.sagansar.todo.controller.mapper.FileMapper;
import com.sagansar.todo.controller.mapper.TaskMapper;
import com.sagansar.todo.controller.util.AccountError;
import com.sagansar.todo.controller.util.ErrorView;
import com.sagansar.todo.controller.util.HttpStatusError;
import com.sagansar.todo.infrastructure.exceptions.BadRequestException;
import com.sagansar.todo.model.work.TaskFile;
import com.sagansar.todo.model.work.TodoTask;
import com.sagansar.todo.repository.TodoTaskRepository;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class MainController implements ErrorController {

    private final UserDetailsServiceImpl userDetailsService;

    private final SecurityService securityService;

    private final TodoTaskRepository todoTaskRepository;

    @RequestMapping(path = "/login")
    public String login(@RequestParam(name = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", error);
        }
        return "login.html";
    }

    @RequestMapping(path = "/my")
    public ModelAndView my(ModelAndView modelAndView) {
        Set<String> roles = userDetailsService.getCurrentUserRoles();
        if (roles.contains("ADMIN")) {
            modelAndView.setViewName("admin-component");
        }
        else if (roles.contains("MANAGER")) {
            if (securityService.isManagerProfileDeleted()) {
                return blocked();
            }
            modelAndView.addObject("supervisor", roles.contains("SUPERVISOR"));
            modelAndView.setViewName("manager.component");
        } else if (roles.contains("FREELANCER")) {
            if (securityService.isWorkerProfileDeleted()) {
                return blocked();
            }
            modelAndView.setViewName("worker.component");
        }
        return modelAndView;
    }

    @RequestMapping(path = "/")
    public String redirectFromRoot() {
        return "redirect:/my";
    }

    @PreAuthorize("hasAnyAuthority('FREELANCER', 'ADMIN')")
    @RequestMapping("/tasks")
    public ModelAndView tasks(ModelAndView modelAndView) {
        modelAndView.setViewName("task-pool");
        return internalPage(modelAndView);
    }

    @RequestMapping("/tasks/{id}")
    public ModelAndView task(@PathVariable(name = "id") Long id, ModelAndView modelAndView) throws BadRequestException {
        TodoTask task = todoTaskRepository.findById(id).orElse(null);
        if (task != null) {
            modelAndView.addObject("taskId", task.getId());
            TaskFile videoFile = task.getFiles().stream()
                    .filter(file -> FileMapper.isVideo(file.getName()))
                    .findAny()
                    .orElse(null);
            boolean hasVideo = videoFile != null;
            modelAndView.addObject("video", hasVideo);
            modelAndView.addObject("videoId", (hasVideo ? videoFile.getId() : ""));
            modelAndView.addObject("task", TaskMapper.taskToFull(task));
            modelAndView.setViewName("task");
            return internalPage(modelAndView);
        }
        return error(HttpStatusError.NOT_FOUND);
    }

    @RequestMapping("/invites")
    public ModelAndView invites(ModelAndView modelAndView) {
        modelAndView.setViewName("invites");
        return internalPage(modelAndView);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @RequestMapping("/responses")
    public ModelAndView responses(ModelAndView modelAndView) {
        modelAndView.setViewName("responses");
        return internalPage(modelAndView);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN')")
    @RequestMapping("/workers")
    public ModelAndView workers(ModelAndView modelAndView) {
        modelAndView.setViewName("workers");
        return internalPage(modelAndView);
    }

    @PreAuthorize("hasAnyAuthority('SUPERVISOR', 'ADMIN')")
    @RequestMapping("/unit")
    public ModelAndView unit(ModelAndView modelAndView) {
        modelAndView.setViewName("unit");
        return internalPage(modelAndView);
    }

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request, ModelAndView modelAndView) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        int statusCode = 0;
        if (status != null) {
            statusCode = Integer.parseInt(status.toString());
        }
        HttpStatusError error = HttpStatusError.error(statusCode);
        String message = (throwable == null || !StringUtils.hasText(throwable.getMessage())) ? error.getMessage() : throwable.getMessage();
        return error(message, error);
    }

    @RequestMapping("/console")
    public ModelAndView adminConsole(ModelAndView modelAndView) {
        if (securityService.isAdmin()) {
            modelAndView.setViewName("console");
            return internalPage(modelAndView);
        }
        return error("Этот раздел доступен только администратору! Вы можете вернуться на домашнюю страницу или обратиться к администратору за помощью",
                HttpStatusError.FORBIDDEN);
    }

    private ModelAndView blocked() {
        if (securityService.isUserBlocked()) {
            return error("Этот аккаунт был заблокирован. Обратитесь к администратору", AccountError.USER_BLOCKED);
        }
        return error("Ваш профиль был удален, но его можно восстановить, нажав на кнопку ниже", AccountError.PROFILE_DELETED);
    }

    private ModelAndView error(HttpStatusError error) {
        return error(error.getMessage(), error);
    }

    private ModelAndView error(String message, ErrorView error) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("title", error.getTitle());
        modelAndView.addObject("errorClass", error.getSelector());
        modelAndView.addObject("header", error.getHeader());
        modelAndView.addObject("message", message);
        modelAndView.setViewName("error");
        return modelAndView;
    }

    private ModelAndView internalPage(ModelAndView modelAndView) {
        Set<String> roles = userDetailsService.getCurrentUserRoles();
        String role = null;
        boolean supervisor = roles.contains("SUPERVISOR");
        if (roles.contains("ADMIN")) {
            role = "ADMIN";
        } else if (roles.contains("MANAGER")) {
            if (securityService.isManagerProfileDeleted()) {
                return blocked();
            }
            role = "MANAGER";
        } else if (roles.contains("FREELANCER")) {
            if (securityService.isWorkerProfileDeleted()) {
                return blocked();
            }
            role = "FREELANCER";
        }
        if (role == null) {
            throw new AccessDeniedException("Доступ ограничен!");
        }
        modelAndView.addObject("userRole", role);
        modelAndView.addObject("supervisor", supervisor);
        return modelAndView;
    }
}
