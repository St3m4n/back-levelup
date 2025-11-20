package com.levelup.backend.service;

import com.levelup.backend.dto.ContactMessageDto;
import com.levelup.backend.dto.CreateContactMessageRequest;
import com.levelup.backend.dto.UpdateContactMessageRequest;
import com.levelup.backend.model.ContactMessage;
import com.levelup.backend.model.ContactMessageStatus;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.repository.ContactMessageRepository;
import com.levelup.backend.security.LevelUpUserDetails;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ContactMessageService {
    private final ContactMessageRepository repository;

    @Transactional(readOnly = true)
    public List<ContactMessageDto> list(String status, String query) {
        requireAdmin();
        Specification<ContactMessage> spec = safeCombine(null, statusSpec(status));
        spec = safeCombine(spec, querySpec(query));
        List<ContactMessage> messages = spec == null
                ? repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : repository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        return messages.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ContactMessageDto create(CreateContactMessageRequest request) {
        ContactMessage message = ContactMessage.builder()
                .id(UUID.randomUUID().toString())
                .nombre(request.getNombre().trim())
                .email(request.getEmail().trim().toLowerCase(Locale.ROOT))
                .asunto(request.getAsunto().trim())
                .mensaje(request.getMensaje().trim())
                .status(ContactMessageStatus.pendiente)
                .build();
        return toDto(repository.save(message));
    }

    @Transactional
    public ContactMessageDto update(String id, UpdateContactMessageRequest request) {
        requireAdmin();
        ContactMessage message = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mensaje no encontrado"));
        if (request.getStatus() != null) {
            ContactMessageStatus resolved = tryParseStatus(request.getStatus());
            if (resolved == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
            }
            message.setStatus(resolved);
        }
        if (request.getRespuesta() != null) {
            message.setRespuesta(trimToNull(request.getRespuesta()));
        }
        return toDto(repository.save(message));
    }

    private Specification<ContactMessage> statusSpec(String status) {
        ContactMessageStatus resolved = tryParseStatus(status);
        if (resolved == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), resolved);
    }

    private Specification<ContactMessage> querySpec(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
        return (root, q, builder) -> builder.or(
                builder.like(builder.lower(root.get("nombre")), pattern),
                builder.like(builder.lower(root.get("email")), pattern),
                builder.like(builder.lower(root.get("asunto")), pattern),
                builder.like(builder.lower(root.get("mensaje")), pattern)
        );
    }

    private Specification<ContactMessage> safeCombine(Specification<ContactMessage> base, Specification<ContactMessage> addition) {
        if (base == null) {
            return addition;
        }
        return addition == null ? base : base.and(addition);
    }

    private ContactMessageDto toDto(ContactMessage message) {
        return ContactMessageDto.builder()
                .id(message.getId())
                .nombre(message.getNombre())
                .email(message.getEmail())
                .asunto(message.getAsunto())
                .mensaje(message.getMensaje())
                .status(message.getStatus())
                .respuesta(message.getRespuesta())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }

    private ContactMessageStatus tryParseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (ContactMessageStatus candidate : ContactMessageStatus.values()) {
            if (candidate.name().equalsIgnoreCase(normalized)) {
                return candidate;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireAdmin() {
        LevelUpUserDetails principal = getPrincipal();
        if (principal == null || !isAdmin(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden acceder");
        }
    }

    private LevelUpUserDetails getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof LevelUpUserDetails details ? details : null;
    }

    private boolean isAdmin(LevelUpUserDetails principal) {
        return principal != null && isAdminProfile(principal.getUser().getPerfil());
    }

    private boolean isAdminProfile(UsuarioPerfil perfil) {
        return perfil == UsuarioPerfil.Administrador || perfil == UsuarioPerfil.Vendedor;
    }
}
