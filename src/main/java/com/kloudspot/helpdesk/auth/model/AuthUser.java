package com.kloudspot.helpdesk.auth.model;

import com.kloudspot.helpdesk.user.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser implements UserDetails {

    private final Long userId;
    private final Long organizationId;
    private final String email;
    private final String password;
    private final Role role;

    public AuthUser(Long userId, Long organizationId, String email, String password, Role role) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    public boolean isOrgAdmin() {
        return role == Role.ORG_ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
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
