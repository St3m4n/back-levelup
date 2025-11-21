package com.levelup.backend.service;

import com.levelup.backend.dto.levelup.LevelUpExpDto;
import com.levelup.backend.dto.levelup.LevelUpReferralDto;
import com.levelup.backend.dto.levelup.LevelUpReferralRequest;
import com.levelup.backend.dto.levelup.LevelUpReferralResponse;
import com.levelup.backend.dto.levelup.LevelUpReferidosDto;
import com.levelup.backend.dto.levelup.LevelUpStatsDto;
import com.levelup.backend.dto.levelup.PurchasePointsRequest;
import com.levelup.backend.dto.levelup.PurchasePointsResponse;
import com.levelup.backend.model.AuditSeverity;
import com.levelup.backend.model.LevelUpStats;
import com.levelup.backend.model.Referral;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.LevelUpStatsRepository;
import com.levelup.backend.repository.ReferralRepository;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.util.EmailUtils;
import com.levelup.backend.util.RunUtils;
import com.levelup.backend.util.SecurityUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LevelUpStatsService {
    private static final int REFERIDO_USA_CODIGO = 100;
    private static final int REFERENTE_GANA = 100;
    private static final int COMPRA_POR_1000 = 1;
    private static final String REF_PREFIX = "LUG-";
    private static final String REF_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int REF_CODE_LENGTH = 6;
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

    private final LevelUpStatsRepository statsRepository;
    private final ReferralRepository referralRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditEventService auditEventService;

    @Transactional
    public LevelUpStats ensureStatsForUsuario(Usuario usuario) {
        return statsRepository.findByUsuarioRun(usuario.getRun())
                .orElseGet(() -> createStats(usuario));
    }

    @Transactional(readOnly = true)
    public LevelUpStatsDto getStats(String run) {
        authorize(run);
        LevelUpStats stats = loadStatsByRun(run);
        return toDto(stats);
    }

    @Transactional
    public LevelUpStatsDto fetchStats(String run) {
        LevelUpStats stats = loadStatsByRun(run);
        return toDto(stats);
    }

    @Transactional
    public LevelUpReferralResponse applyReferral(LevelUpReferralRequest request) {
        return executeReferral(request, null);
    }

    @Transactional
    public LevelUpReferralResponse applyReferral(LevelUpReferralRequest request, String actorOverride) {
        return executeReferral(request, actorOverride);
    }

    @Transactional
    public LevelUpReferralResponse applyReferralOnRegistration(String run,
                                                               String referralCode,
                                                               String email) {
        LevelUpReferralRequest request = LevelUpReferralRequest.builder()
                .newRun(run)
                .referralCode(referralCode)
                .newEmail(email)
                .build();
        return executeReferral(request, null);
    }

    @Transactional
    public PurchasePointsResponse addPurchasePoints(PurchasePointsRequest request) {
        return addPurchasePoints(request, null);
    }

    @Transactional
    public PurchasePointsResponse addPurchasePoints(PurchasePointsRequest request, String actorOverride) {
        String runKey = normalizeRun(request.getRun());
        if (runKey.isBlank()) {
            return PurchasePointsResponse.builder()
                    .ok(true)
                    .pointsAdded(0)
                    .totalPoints(0)
                    .stats(null)
                    .build();
        }
        Usuario usuario = usuarioRepository.findByRun(runKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + runKey));
        LevelUpStats stats = ensureStatsForUsuario(usuario);
        int pointsToAdd = calculatePoints(request.getTotalCLP());
        if (pointsToAdd > 0) {
            stats.setPoints(stats.getPoints() + pointsToAdd);
            stats.setExpCompras(stats.getExpCompras() + pointsToAdd);
            stats.setUpdatedAt(LocalDateTime.now());
            statsRepository.save(stats);
        }
        auditEventService.logEvent(actorOverride,
                "LEVELUP_PURCHASE",
                "LevelUpStats",
                runKey,
                AuditSeverity.LOW,
                String.format("Se sumaron %d puntos por compra", pointsToAdd),
                Map.of("run", runKey, "pointsAdded", pointsToAdd, "totalCLP", request.getTotalCLP()));
        LevelUpStatsDto dto = toDto(stats);
        return PurchasePointsResponse.builder()
                .ok(true)
                .pointsAdded(pointsToAdd)
                .totalPoints(stats.getPoints())
                .stats(dto)
                .build();
    }

    private LevelUpStatsDto toDto(LevelUpStats stats) {
        List<LevelUpReferralDto> referrals = referralRepository.findByStatsOrderByCreatedAtDesc(stats)
                .stream()
                .map(entry -> LevelUpReferralDto.builder()
                        .email(entry.getEmail())
                        .date(entry.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return LevelUpStatsDto.builder()
                .run(stats.getRun())
                .points(stats.getPoints())
                .exp(LevelUpExpDto.builder()
                        .compras(stats.getExpCompras())
                        .torneos(stats.getExpTorneos())
                        .referidos(stats.getExpReferidos())
                        .build())
                .referralCode(stats.getReferralCode())
                .referredBy(stats.getReferredBy())
                .referidos(LevelUpReferidosDto.builder()
                        .count(referrals.size())
                        .users(referrals)
                        .build())
                .updatedAt(stats.getUpdatedAt())
                .build();
    }

    private LevelUpStats createStats(Usuario usuario) {
        LevelUpStats stats = LevelUpStats.builder()
                .usuario(usuario)
                .run(usuario.getRun())
                .points(0)
                .expCompras(0)
                .expTorneos(0)
                .expReferidos(0)
                .referralCode(generateUniqueReferralCode())
                .build();
        return statsRepository.save(stats);
    }

    private LevelUpReferralResponse executeReferral(LevelUpReferralRequest request, String actorOverride) {
        String normalizedRun = normalizeRun(request.getNewRun());
        if (normalizedRun.isBlank()) {
            return failure("invalid-run");
        }
        String code = normalizeReferralCode(request.getReferralCode());
        if (code.isBlank()) {
            return failure("no-code");
        }
        Usuario usuario = usuarioRepository.findByRun(normalizedRun)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + normalizedRun));
        LevelUpStats target = ensureStatsForUsuario(usuario);
        if (target.getReferredBy() != null && !target.getReferredBy().isBlank()) {
            return failure("already-referred");
        }
        LevelUpStats owner = statsRepository.findByReferralCode(code)
                .orElse(null);
        if (owner == null) {
            return failure("code-not-found");
        }
        if (owner.getRun().equals(normalizedRun)) {
            return failure("self-ref");
        }
        LocalDateTime now = LocalDateTime.now();
        target.setPoints(target.getPoints() + REFERIDO_USA_CODIGO);
        target.setExpReferidos(target.getExpReferidos() + REFERIDO_USA_CODIGO);
        target.setReferredBy(owner.getRun());
        target.setUpdatedAt(now);

        owner.setPoints(owner.getPoints() + REFERENTE_GANA);
        owner.setExpReferidos(owner.getExpReferidos() + REFERENTE_GANA);
        owner.setUpdatedAt(now);

        String normalizedEmail = EmailUtils.normalizeCorreo(request.getNewEmail());
        if (!normalizedEmail.isBlank() && !referralRepository.existsByStatsAndEmail(owner, normalizedEmail)) {
            Referral referral = Referral.builder()
                    .stats(owner)
                    .email(normalizedEmail)
                    .build();
            owner.getReferrals().add(referral);
        }
        statsRepository.saveAll(List.of(target, owner));
        auditEventService.logEvent(actorOverride,
                "LEVELUP_REFERRAL",
                "LevelUpStats",
                normalizedRun,
                AuditSeverity.LOW,
                String.format("Se aplicó referido %s -> %s", owner.getRun(), normalizedRun),
                Map.of("newRun", normalizedRun,
                        "ownerRun", owner.getRun(),
                        "referralCode", code,
                        "newEmail", normalizedEmail));
        return LevelUpReferralResponse.builder()
                .ok(true)
                .newUserPoints(REFERIDO_USA_CODIGO)
                .referrerPoints(REFERENTE_GANA)
                .refRun(owner.getRun())
                .build();
    }

    private int calculatePoints(BigDecimal totalCLP) {
        if (totalCLP == null) {
            return 0;
        }
        BigDecimal normalized = totalCLP.max(BigDecimal.ZERO);
        int base = normalized.divideToIntegralValue(THOUSAND).intValue();
        return base * COMPRA_POR_1000;
    }

    private String normalizeRun(String run) {
        return RunUtils.normalizeRun(run);
    }

    private String normalizeReferralCode(String code) {
        if (code == null) {
            return "";
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private LevelUpReferralResponse failure(String reason) {
        return LevelUpReferralResponse.builder()
                .ok(false)
                .reason(reason)
                .build();
    }

    private void authorize(String run) {
        String normalizedRun = normalizeRun(run);
        String actorRun = SecurityUtils.getCurrentRun().orElse("");
        boolean admin = SecurityUtils.isCurrentUserAdminOrVendor();
        if (!admin && !actorRun.equals(normalizedRun)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    private LevelUpStats loadStatsByRun(String run) {
        String normalizedRun = normalizeRun(run);
        if (normalizedRun.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + run);
        }
        Usuario usuario = usuarioRepository.findByRun(normalizedRun)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado: " + normalizedRun));
        return ensureStatsForUsuario(usuario);
    }

    private String generateUniqueReferralCode() {
        String candidate;
        do {
            candidate = buildReferralCode();
        } while (statsRepository.existsByReferralCode(candidate));
        return candidate;
    }

    private String buildReferralCode() {
        StringBuilder builder = new StringBuilder(REF_PREFIX);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < REF_CODE_LENGTH; i++) {
            int index = random.nextInt(REF_ALPHABET.length());
            builder.append(REF_ALPHABET.charAt(index));
        }
        return builder.toString();
    }
}
