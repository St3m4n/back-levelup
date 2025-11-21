package com.levelup.backend.util;

import com.levelup.backend.model.Usuario;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.security.LevelUpUserDetails;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static Optional<LevelUpUserDetails> getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LevelUpUserDetails details) {
            return Optional.of(details);
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentRun() {
        return getCurrentUserDetails().map(details -> details.getUser().getRun());
    }

    public static boolean isCurrentUserAdmin() {
        return getCurrentUserDetails()
            .map(LevelUpUserDetails::getUser)
            .map(user -> user.getPerfil())
            .map(perfil -> perfil == UsuarioPerfil.Administrador)
            .orElse(false);
    }

    public static boolean isCurrentUserSuperAdmin() {
        return getCurrentUserDetails()
            .map(LevelUpUserDetails::getUser)
            .map(Usuario::isSystemAccount)
            .orElse(false);
    }

    public static boolean isCurrentUserAdminOrVendor() {
        return getCurrentUserDetails()
            .map(LevelUpUserDetails::getUser)
            .map(user -> user.getPerfil())
            .map(perfil -> perfil == UsuarioPerfil.Administrador || perfil == UsuarioPerfil.Vendedor)
            .orElse(false);
    }
}
