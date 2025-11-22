package com.levelup.backend.dto;

import com.levelup.backend.dto.levelup.LevelUpStatsDto;
import com.levelup.backend.model.UsuarioPerfil;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
    private final String run;
    private final String nombre;
    private final String apellidos;
    private final String correo;
    private final UsuarioPerfil perfil;
    private final LocalDate fechaNacimiento;
    private final String region;
    private final String comuna;
    private final String direccion;
    private final boolean descuentoVitalicio;
    private final boolean systemAccount;
    private final String referralCode;
    private final LevelUpStatsDto levelUpStats;
}
