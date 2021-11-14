package com.sagansar.todo.service;

import com.sagansar.todo.model.general.RoleEnum;
import com.sagansar.todo.model.general.User;
import com.sagansar.todo.model.general.UserAuthDetails;
import com.sagansar.todo.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username);
        }
        return new UserAuthDetails(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Set<String> getCurrentUserRoles() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserDetails user;

        if (principal instanceof UserDetails) {
            user = (UserDetails) principal;
        } else {
            throw new RuntimeException("Unable to load user!");
        }

        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UserDetails user;

        if (principal instanceof UserDetails) {
            user = (UserDetails) principal;
        } else {
            throw new RuntimeException("Unable to load user!");
        }
        return userRepository.findByUsername(user.getUsername());
    }

    public boolean checkUserRights(RoleEnum role) {
        Set<String> currentUserRoles = getCurrentUserRoles();
        return (currentUserRoles.contains(role.name()) || currentUserRoles.contains("ADMIN"));
    }

    public boolean isAdmin() {
        Set<String> currentUserRoles = getCurrentUserRoles();
        return currentUserRoles.contains("ADMIN");
    }
}
