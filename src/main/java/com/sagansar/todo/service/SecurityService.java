package com.sagansar.todo.service;

import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class SecurityService {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

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
}
