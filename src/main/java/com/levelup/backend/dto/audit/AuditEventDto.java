package com.levelup.backend.dto.audit;

import com.levelup.backend.model.AuditSeverity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditEventDto {
    private final Long id;
    private final String actor;
    private final String action;
    private final String entityType;
    private final String entityId;
    private final String summary;
    private final AuditSeverity severity;
    private final String metadata;
    private final LocalDateTime createdAt;
}
