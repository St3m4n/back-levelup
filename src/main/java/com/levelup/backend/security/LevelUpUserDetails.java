package com.levelup.backend.security;

import com.levelup.backend.model.Usuario;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class LevelUpUserDetails implements UserDetails {
    private final Usuario user;

    public LevelUpUserDetails(Usuario user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getPerfil().name().toUpperCase()));
        if (user.isSystemAccount()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPERADMIN"));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getCorreo();
    }

    @Override
    public boolean isAccountNonExpired() {
        return user.getDeletedAt() == null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getDeletedAt() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return user.getDeletedAt() == null;
    }

    @Override
    public boolean isEnabled() {
        return user.getDeletedAt() == null;
    }
}
