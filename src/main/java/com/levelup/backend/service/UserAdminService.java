package com.levelup.backend.service;

import com.levelup.backend.dto.levelup.LevelUpReferralDto;
import com.levelup.backend.dto.levelup.LevelUpReferidosDto;
import com.levelup.backend.dto.user.UserDto;
import com.levelup.backend.model.LevelUpStats;
import com.levelup.backend.model.Referral;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.LevelUpStatsRepository;
import com.levelup.backend.repository.ReferralRepository;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.repository.UsuarioSpecifications;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAdminService {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "run", "nombre", "apellidos", "correo", "region", "comuna", "createdAt", "updatedAt");
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    private final UsuarioRepository usuarioRepository;
    private final LevelUpStatsRepository levelUpStatsRepository;
    private final ReferralRepository referralRepository;

    public Page<UserDto> listUsers(Boolean activo,
                                   String region,
                                   String search,
                                   String sortBy,
                                   String order,
                                   int page,
                                   int limit) {
        int safePage = Math.max(page, 1);
        int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
        String requestedSort = sortBy != null ? sortBy : DEFAULT_SORT_FIELD;
        String safeSort = ALLOWED_SORT_FIELDS.contains(requestedSort) ? requestedSort : DEFAULT_SORT_FIELD;
        String normalizedOrder = order != null ? order : "";
        Sort.Direction direction = "desc".equalsIgnoreCase(normalizedOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage - 1, safeLimit, Sort.by(direction, safeSort));

        Specification<Usuario> spec = combine(UsuarioSpecifications.activo(activo),
            UsuarioSpecifications.regionEquals(region));
        spec = combine(spec, UsuarioSpecifications.search(search));

        Page<Usuario> usuarios = usuarioRepository.findAll(spec, pageable);
        List<Usuario> content = usuarios.getContent();
        if (content.isEmpty()) {
            return new PageImpl<>(Objects.requireNonNull(List.<UserDto>of()), pageable, usuarios.getTotalElements());
        }

        Set<String> runs = content.stream().map(Usuario::getRun).collect(Collectors.toSet());
        Map<String, LevelUpStats> statsByRun = levelUpStatsRepository.findByRunIn(runs).stream()
            .collect(Collectors.toMap(LevelUpStats::getRun, stats -> stats));
        List<LevelUpStats> statsList = statsByRun.values().stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        Map<Long, List<Referral>> referralsByStatsId = statsList.isEmpty()
            ? Map.of()
            : referralRepository.findByStatsInOrderByCreatedAtDesc(statsList)
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getStats().getId(), Collectors.toList()));

        List<UserDto> users = content.stream()
            .map(usuario -> {
                LevelUpStats stats = statsByRun.get(usuario.getRun());
                List<Referral> referrals = stats != null
                        ? referralsByStatsId.getOrDefault(stats.getId(), List.of())
                        : List.of();
                return toDto(usuario, stats, referrals);
            })
            .collect(Collectors.toList());
        return new PageImpl<>(Objects.requireNonNull(List.copyOf(users)), pageable, usuarios.getTotalElements());
    }

    private Specification<Usuario> combine(Specification<Usuario> base, Specification<Usuario> addition) {
        if (addition == null) {
            return base;
        }
        return base == null ? addition : base.and(addition);
    }

    private UserDto toDto(Usuario usuario, LevelUpStats stats, List<Referral> referrals) {
        List<String> roles = new ArrayList<>();
        roles.add(usuario.getPerfil().name());
        if (usuario.isSystemAccount()) {
            roles.add("SUPERADMIN");
        }
        return UserDto.builder()
                .run(usuario.getRun())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .apellidos(usuario.getApellidos())
                .telefono(null)
                .direccion(usuario.getDireccion())
                .region(usuario.getRegion())
                .comuna(usuario.getComuna())
                .nivel(determineLevel(stats))
                .activo(usuario.isActive())
                .fechaRegistro(usuario.getCreatedAt())
                .roles(roles)
                .ultimoIngreso(null)
                .puntosLevelUp(stats != null ? stats.getPoints() : 0)
                .perfil(usuario.getPerfil().name())
                .referidos(buildReferidosDto(referrals))
                .build();
    }

    private int determineLevel(LevelUpStats stats) {
        if (stats == null) {
            return 1;
        }
        return Math.max(1, stats.getPoints() / 1000 + 1);
    }

    private LevelUpReferidosDto buildReferidosDto(List<Referral> referrals) {
        if (referrals == null || referrals.isEmpty()) {
            return LevelUpReferidosDto.builder()
                    .count(0)
                    .users(List.of())
                    .build();
        }
        List<LevelUpReferralDto> users = referrals.stream()
                .map(entry -> LevelUpReferralDto.builder()
                        .email(entry.getEmail())
                        .date(entry.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return LevelUpReferidosDto.builder()
                .count(users.size())
                .users(users)
                .build();
    }
}