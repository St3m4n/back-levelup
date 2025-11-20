package com.levelup.backend.service;

import com.levelup.backend.dto.UserProfileDto;
import com.levelup.backend.dto.auth.AuthResponse;
import com.levelup.backend.dto.auth.LoginRequest;
import com.levelup.backend.dto.auth.RegisterRequest;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.JwtTokenProvider;
import com.levelup.backend.security.LevelUpUserDetails;
import com.levelup.backend.util.PasswordUtils;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider tokenProvider;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String correo = normalizeCorreo(request.getCorreo());
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        if (usuario.getPasswordHash() == null || usuario.getPasswordSalt() == null) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        String computed = PasswordUtils.hashPassword(usuario.getPasswordSalt(), request.getPassword());
        if (!computed.equals(usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
        LevelUpUserDetails details = new LevelUpUserDetails(usuario);
        String token = tokenProvider.generateToken(details);
        return buildResponse(usuario, token);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String correo = normalizeCorreo(request.getCorreo());
        String run = normalizeRun(request.getRun());
        if (correo.isBlank() || run.isBlank()) {
            throw new IllegalArgumentException("RUN y correo son obligatorios");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
        if (usuarioRepository.existsByRun(run)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese RUN");
        }
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(salt, request.getPassword());
        Usuario nuevo = Usuario.builder()
                .run(run)
                .nombre(request.getNombre().trim())
                .apellidos(request.getApellidos().trim())
                .correo(correo)
                .perfil(UsuarioPerfil.Cliente)
                .fechaNacimiento(parseFecha(request.getFechaNacimiento()))
                .region(request.getRegion().trim())
                .comuna(request.getComuna().trim())
                .direccion(request.getDireccion().trim())
                .descuentoVitalicio(correoEndsDuoc(correo))
                .systemAccount(false)
                .passwordHash(hash)
                .passwordSalt(salt)
                .build();
        usuarioRepository.save(nuevo);
        LevelUpUserDetails details = new LevelUpUserDetails(nuevo);
        String token = tokenProvider.generateToken(details);
        return buildResponse(nuevo, token);
    }

    private UserProfileDto toDto(Usuario usuario) {
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

    private AuthResponse buildResponse(Usuario usuario, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toDto(usuario))
                .build();
    }

    private String normalizeCorreo(String correo) {
        if (correo == null) {
            return "";
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRun(String run) {
        if (run == null) {
            return "";
        }
        return run.replaceAll("[^0-9kK]", "").toUpperCase(Locale.ROOT);
    }

    private boolean correoEndsDuoc(String correo) {
        return correo.endsWith("@duoc.cl");
    }

    private LocalDate parseFecha(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Fecha de nacimiento inválida", ex);
        }
    }
}
