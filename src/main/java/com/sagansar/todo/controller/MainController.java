package com.sagansar.todo.controller;

import com.sagansar.todo.controller.dto.UserDto;
import com.sagansar.todo.repository.RoleRepository;
import com.sagansar.todo.repository.UserRepository;
import com.sagansar.todo.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class MainController {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(path = "/login")
    public String login(@RequestParam(name = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("loginError", error);
        }
        return "login.html";
    }

    @RequestMapping(path = "/my")
    public String my() {

        Set<String> roles = userDetailsService.getCurrentUserRoles();
        if (roles.contains("ADMIN")) {
            return "admin.component.html";
        }
        if (roles.contains("MANAGER")) {
            return "manager.component.html";
        }
        return "worker.component.html";
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
}
