package com.levelup.backend.dto.audit;

import com.levelup.backend.model.AuditSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEventRequest {
    @NotBlank
    private String action;

    private String entityType;

    private String entityId;

    private String summary;

    @NotNull
    private AuditSeverity severity;

    private Map<String, Object> metadata;
}
