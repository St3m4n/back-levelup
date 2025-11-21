package com.levelup.backend.config;

import com.levelup.backend.model.Usuario;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.service.LevelUpStatsService;
import com.levelup.backend.util.PasswordUtils;
import com.levelup.backend.util.RunUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminSeeder implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;
    private final LevelUpStatsService levelUpStatsService;

    @Value("${levelup.superadmin.run:00000000}")
    private String superAdminRun;

    @Value("${levelup.superadmin.email:superadmin@levelup.com}")
    private String superAdminEmail;

    @Value("${levelup.superadmin.password:SuperAdmin123!}")
    private String superAdminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        String normalizedRun = RunUtils.normalizeRun(superAdminRun);
        if (normalizedRun.isBlank()) {
            log.warn("Super admin run is blank; skipping seeding");
            return;
        }
        if (usuarioRepository.existsByRun(normalizedRun)) {
            log.debug("Super admin {} already exists", normalizedRun);
            return;
        }
        String correo = superAdminEmail != null ? superAdminEmail.trim().toLowerCase() : "";
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(salt, superAdminPassword);
        Usuario superAdmin = Usuario.builder()
                .run(normalizedRun)
                .nombre("Super")
                .apellidos("Administrador")
                .correo(correo)
                .perfil(UsuarioPerfil.Administrador)
                .region("Metropolitana de Santiago")
                .comuna("Santiago Centro")
                .direccion("Nivel Up HQ")
                .descuentoVitalicio(true)
                .systemAccount(true)
                .passwordHash(hash)
                .passwordSalt(salt)
                .build();
        usuarioRepository.save(superAdmin);
        levelUpStatsService.ensureStatsForUsuario(superAdmin);
        log.info("Super admin {} seeded with correo {}", normalizedRun, correo);
    }
}