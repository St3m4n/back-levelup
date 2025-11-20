package com.levelup.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{7,8}[0-9Kk]$", message = "RUN inválido")
    private String run;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellidos;

    @Email
    @NotBlank
    private String correo;

    private String fechaNacimiento;

    @NotBlank
    private String region;

    @NotBlank
    private String comuna;

    @NotBlank
    private String direccion;

    @NotBlank
    @Size(min = 8)
    private String password;

    private String referralCode;
}
