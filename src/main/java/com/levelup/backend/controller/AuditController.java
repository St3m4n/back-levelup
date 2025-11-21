package com.levelup.backend.controller;

import com.levelup.backend.dto.audit.AuditEventDto;
import com.levelup.backend.dto.audit.AuditEventRequest;
import com.levelup.backend.service.AuditEventService;
import com.levelup.backend.util.SecurityUtils;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {
    private final AuditEventService auditEventService;

    @GetMapping
    public ResponseEntity<List<AuditEventDto>> search(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "100") int limit) {
        requireAdminOrSuperAdmin();
        List<AuditEventDto> events = auditEventService.search(action, entityType, from, to, limit);
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody AuditEventRequest request) {
        requireAdminOrSuperAdmin();
        auditEventService.logEvent(request.getAction(),
                request.getEntityType(),
                request.getEntityId(),
                request.getSeverity(),
                request.getSummary(),
                request.getMetadata());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> purge() {
        requireAdminOrSuperAdmin();
        auditEventService.purge();
        return ResponseEntity.noContent().build();
    }

    private void requireAdminOrSuperAdmin() {
        if (!SecurityUtils.isCurrentUserAdmin() && !SecurityUtils.isCurrentUserSuperAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }
}
