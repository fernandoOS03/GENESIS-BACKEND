package com.goodNews.genesis.core.security;

import com.goodNews.genesis.shared.enums.UsersEnum;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class CustomUserDetails implements UserDetails {
    private final UUID userId;
    private final String email;
    private final UsersEnum rol;
    private final String pais;

    public CustomUserDetails(UUID userId, String email, UsersEnum rol, String pais) {
        this.userId = userId;
        this.email = email;
        this.rol = rol;
        this.pais = pais;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(rol.name()));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public UUID getUserId() {
        return userId;
    }

    public UsersEnum getRol() {
        return rol;
    }

    public String getPais() {
        return pais;
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
