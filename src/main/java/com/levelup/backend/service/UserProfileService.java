package com.levelup.backend.service;

import com.levelup.backend.dto.UserProfileDto;
import com.levelup.backend.security.LevelUpUserDetails;
import com.levelup.backend.util.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {
    public UserProfileDto getCurrentProfile() {
        LevelUpUserDetails principal = SecurityUtils.getCurrentUserDetails()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        return toDto(principal);
    }

    private UserProfileDto toDto(LevelUpUserDetails principal) {
        var usuario = principal.getUser();
        return UserProfileDto.builder()
                .run(usuario.getRun())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .perfil(usuario.getPerfil())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .region(usuario.getRegion())
                .comuna(usuario.getComuna())
                .direccion(usuario.getDireccion())
                .descuentoVitalicio(usuario.isDescuentoVitalicio())
                .systemAccount(usuario.isSystemAccount())
                .build();
    }
}
