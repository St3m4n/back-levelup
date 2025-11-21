package com.levelup.backend.dto.user;

import com.levelup.backend.dto.levelup.LevelUpReferidosDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String run;
    private String correo;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String direccion;
    private String region;
    private String comuna;
    private Integer nivel;
    private boolean activo;
    private LocalDateTime fechaRegistro;
    private List<String> roles;
    private LocalDateTime ultimoIngreso;
    private Integer puntosLevelUp;
    private String perfil;
    private LevelUpReferidosDto referidos;
}