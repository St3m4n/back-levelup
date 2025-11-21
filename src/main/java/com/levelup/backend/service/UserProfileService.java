package com.levelup.backend.service;

import com.levelup.backend.dto.UserProfileDto;
import com.levelup.backend.dto.levelup.LevelUpStatsDto;
import com.levelup.backend.security.LevelUpUserDetails;
import com.levelup.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final LevelUpStatsService levelUpStatsService;

    public UserProfileDto getCurrentProfile() {
        LevelUpUserDetails principal = SecurityUtils.getCurrentUserDetails()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        LevelUpStatsDto stats = levelUpStatsService.getStats(principal.getUser().getRun());
        return toDto(principal, stats);
    }

    private UserProfileDto toDto(LevelUpUserDetails principal, LevelUpStatsDto stats) {
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
                .levelUpStats(stats)
                .build();
    }
}
