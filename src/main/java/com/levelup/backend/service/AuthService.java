package com.levelup.backend.service;

import com.levelup.backend.dto.UserProfileDto;
import com.levelup.backend.dto.auth.AuthResponse;
import com.levelup.backend.dto.auth.LoginRequest;
import com.levelup.backend.dto.auth.RegisterRequest;
import com.levelup.backend.dto.levelup.LevelUpReferralResponse;
import com.levelup.backend.dto.levelup.LevelUpStatsDto;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.JwtTokenProvider;
import com.levelup.backend.security.LevelUpUserDetails;
import com.levelup.backend.util.EmailUtils;
import com.levelup.backend.util.PasswordUtils;
import com.levelup.backend.util.RunUtils;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider tokenProvider;
    private final LevelUpStatsService levelUpStatsService;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String correo = EmailUtils.normalizeCorreo(request.getCorreo());
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
        LevelUpStatsDto stats = levelUpStatsService.fetchStats(usuario.getRun());
        return buildResponse(usuario, token, stats);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String correo = EmailUtils.normalizeCorreo(request.getCorreo());
        String run = RunUtils.normalizeRun(request.getRun());
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
        levelUpStatsService.ensureStatsForUsuario(nuevo);
        String referralCode = request.getReferralCode();
        if (referralCode != null && !referralCode.isBlank()) {
            LevelUpReferralResponse referral = levelUpStatsService.applyReferralOnRegistration(run, referralCode,
                    correo);
            if (!referral.isOk()) {
                log.warn("No se aplicó código de referido {} para {}: {}", referralCode, run, referral.getReason());
            }
        }
        LevelUpStatsDto stats = levelUpStatsService.fetchStats(nuevo.getRun());
        LevelUpUserDetails details = new LevelUpUserDetails(nuevo);
        String token = tokenProvider.generateToken(details);
        return buildResponse(nuevo, token, stats);
    }

    private UserProfileDto toDto(Usuario usuario, LevelUpStatsDto levelUpStats) {
        String referralCode = levelUpStats != null ? levelUpStats.getReferralCode() : null;
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
                .referralCode(referralCode)
                .levelUpStats(levelUpStats)
                .build();
    }

    private AuthResponse buildResponse(Usuario usuario, String token, LevelUpStatsDto levelUpStats) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(toDto(usuario, levelUpStats))
                .build();
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
