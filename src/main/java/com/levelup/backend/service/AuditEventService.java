package com.levelup.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.backend.dto.audit.AuditEventDto;
import com.levelup.backend.model.AuditEvent;
import com.levelup.backend.model.AuditSeverity;
import com.levelup.backend.repository.AuditEventRepository;
import com.levelup.backend.util.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditEventService {
    private static final String SYSTEM_ACTOR = "SYSTEM";

    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public void logEvent(String action,
                         String entityType,
                         String entityId,
                         AuditSeverity severity,
                         String summary,
                         Map<String, Object> metadata) {
        logEvent(null, action, entityType, entityId, severity, summary, metadata);
    }

    public void logEvent(String actorOverride,
                         String action,
                         String entityType,
                         String entityId,
                         AuditSeverity severity,
                         String summary,
                         Map<String, Object> metadata) {
        String actor = resolveActor(actorOverride);
        AuditEvent event = AuditEvent.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .summary(summary)
                .severity(severity)
                .metadata(serialize(metadata))
                .build();
        auditEventRepository.save(event);
    }

    public List<AuditEventDto> search(String action,
                                      String entityType,
                                      LocalDateTime from,
                                      LocalDateTime to,
                                      int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AuditEvent> cq = cb.createQuery(AuditEvent.class);
        Root<AuditEvent> root = cq.from(AuditEvent.class);
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.hasText(action)) {
            predicates.add(cb.like(cb.lower(root.get("action")), "%" + action.toLowerCase() + "%"));
        }
        if (StringUtils.hasText(entityType)) {
            predicates.add(cb.like(cb.lower(root.get("entityType")), "%" + entityType.toLowerCase() + "%"));
        }
        if (from != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (to != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("createdAt")));
        return entityManager.createQuery(cq)
                .setMaxResults(enforceLimit(limit))
                .getResultStream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void purge() {
        auditEventRepository.deleteAllInBatch();
        auditEventRepository.flush();
        logEvent("SYSTEM",
                "AUDIT_PURGE",
                "AuditEvent",
                null,
                AuditSeverity.MEDIUM,
                "Se purgó el log de auditoría",
                Collections.emptyMap());
    }

    private String resolveActor(String actorOverride) {
        if (actorOverride != null && !actorOverride.isBlank()) {
            return actorOverride.trim();
        }
        return SecurityUtils.getCurrentRun()
                .filter(run -> !run.isBlank())
                .orElse(SYSTEM_ACTOR);
    }

    private String serialize(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException ex) {
            log.warn("No se pudo serializar metadata de auditoría", ex);
            return null;
        }
    }

    private int enforceLimit(int limit) {
        if (limit <= 0) {
            return 100;
        }
        return Math.min(limit, 750);
    }

    private AuditEventDto toDto(AuditEvent event) {
        return AuditEventDto.builder()
                .id(event.getId())
                .actor(event.getActor())
                .action(event.getAction())
                .entityType(event.getEntityType())
                .entityId(event.getEntityId())
                .summary(event.getSummary())
                .severity(event.getSeverity())
                .metadata(event.getMetadata())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
