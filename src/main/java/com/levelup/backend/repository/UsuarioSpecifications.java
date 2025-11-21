package com.levelup.backend.repository;

import com.levelup.backend.model.Usuario;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public final class UsuarioSpecifications {
    private UsuarioSpecifications() {
    }

    public static Specification<Usuario> activo(Boolean activo) {
        if (activo == null) {
            return null;
        }
        return (root, query, criteriaBuilder) -> activo
                ? criteriaBuilder.isNull(root.get("deletedAt"))
                : criteriaBuilder.isNotNull(root.get("deletedAt"));
    }

    public static Specification<Usuario> regionEquals(String region) {
        if (!hasText(region)) {
            return null;
        }
        String normalized = region.trim().toUpperCase(Locale.ROOT);
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                criteriaBuilder.upper(root.get("region")), normalized);
    }

    public static Specification<Usuario> search(String term) {
        if (!hasText(term)) {
            return null;
        }
        String pattern = "%" + term.trim().toUpperCase(Locale.ROOT) + "%";
        return (root, query, criteriaBuilder) -> {
            Predicate run = criteriaBuilder.like(criteriaBuilder.upper(root.get("run")), pattern);
            Predicate nombre = criteriaBuilder.like(criteriaBuilder.upper(root.get("nombre")), pattern);
            Predicate apellidos = criteriaBuilder.like(criteriaBuilder.upper(root.get("apellidos")), pattern);
            Predicate correo = criteriaBuilder.like(criteriaBuilder.upper(root.get("correo")), pattern);
            return criteriaBuilder.or(run, nombre, apellidos, correo);
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}