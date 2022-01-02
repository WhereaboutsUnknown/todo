package com.sagansar.todo.model.general;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserAuthDetails implements UserDetails {

    protected User user;

    public UserAuthDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.user != null) {
            return user.roles;
        }
        return Collections.emptySet();
    }

    @Override
    public String getPassword() {
        if (this.user != null) {
            return user.password;
        }
        return "";
    }

    @Override
    public String getUsername() {
        if (this.user != null) {
            return user.username;
        }
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; //TODO: account blocking
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
