package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.UserDto;
import com.sagansar.todo.controller.util.HttpStatusError;
import com.sagansar.todo.repository.RoleRepository;
import com.sagansar.todo.repository.UserRepository;
import com.sagansar.todo.service.SecurityService;
import com.sagansar.todo.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class MainController implements ErrorController {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            modelAndView.addObject("supervisor", roles.contains("SUPERVISOR"));
            modelAndView.setViewName("manager.component");
        } else if (roles.contains("WORKER")) {
            modelAndView.setViewName("worker.component");
        }
        return modelAndView;
    }

    @RequestMapping(path = "/")
    public String redirectFromRoot() {
        return "redirect:/my";
    }

    /*@PostMapping("/registration")
    @ResponseStatus(code = HttpStatus.CREATED)
    public void register(@RequestBody UserDto userDto, Model model) {
        if (userDto == null) {
            model.addAttribute("emptyRequest", true);
            return model;
        }
    }*/

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
        modelAndView.addObject("httpStatusName", error.getTitle());
        modelAndView.addObject("httpStatus", error.getSelector());
        modelAndView.addObject("header", error.getHeader());
        modelAndView.addObject("message", message);
        modelAndView.setViewName("error");

        return modelAndView;
    }

    @RequestMapping("/console")
    public ModelAndView adminConsole(ModelAndView modelAndView) {
        if (securityService.isAdmin()) {
            modelAndView.setViewName("console");
            return modelAndView;
        }
        throw new AccessDeniedException("Этот раздел доступен только администратору");
    }
}
