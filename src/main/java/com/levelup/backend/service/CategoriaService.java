package com.levelup.backend.service;

import com.levelup.backend.dto.producto.CategoriaDto;
import com.levelup.backend.model.Categoria;
import com.levelup.backend.repository.CategoriaRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    @Transactional
    public Categoria ensureCategory(String rawName) {
        String normalized = normalizeName(rawName);
        return categoriaRepository.findByNombreIgnoreCase(normalized)
                .map(this::reviveIfDeleted)
                .orElseGet(() -> categoriaRepository.save(
                        Categoria.builder().nombre(normalized).build()));
    }

    @Transactional(readOnly = true)
    public List<CategoriaDto> list(boolean includeDeleted) {
        return categoriaRepository.findAll().stream()
                .filter(category -> includeDeleted || category.getDeletedAt() == null)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<CategoriaDto> sync(List<String> names) {
        Map<String, String> normalizedToDisplay = names.stream()
                .map(this::normalizeName)
                .filter(name -> !name.isBlank())
                .collect(Collectors.toMap(
                        name -> name.toLowerCase(Locale.ROOT),
                        name -> name,
                        (first, second) -> first,
                        LinkedHashMap::new));
        Set<String> providedKeys = new LinkedHashSet<>(normalizedToDisplay.keySet());
        List<Categoria> existing = categoriaRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        List<Categoria> toSave = new ArrayList<>();

        Map<String, Categoria> lookup = existing.stream()
                .collect(Collectors.toMap(category -> category.getNombre().toLowerCase(Locale.ROOT), category -> category));

        for (Map.Entry<String, Categoria> entry : lookup.entrySet()) {
            String key = entry.getKey();
            Categoria category = entry.getValue();
            if (!providedKeys.contains(key)) {
                if (category.getDeletedAt() == null) {
                    category.setDeletedAt(now);
                }
                toSave.add(category);
                continue;
            }
            category.setDeletedAt(null);
            toSave.add(category);
            providedKeys.remove(key);
        }

        for (String missing : providedKeys) {
            String displayName = normalizedToDisplay.get(missing);
            Categoria next = Categoria.builder().nombre(displayName).build();
            toSave.add(next);
        }

        List<Categoria> persisted = categoriaRepository.saveAll(toSave);
        return persisted.stream().map(this::toDto).collect(Collectors.toList());
    }

    private Categoria reviveIfDeleted(Categoria category) {
        if (category.getDeletedAt() != null) {
            category.setDeletedAt(null);
            return categoriaRepository.save(category);
        }
        return category;
    }

    private CategoriaDto toDto(Categoria categoria) {
        return CategoriaDto.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .eliminada(categoria.getDeletedAt() != null)
                .createdAt(categoria.getCreatedAt())
                .updatedAt(categoria.getUpdatedAt())
                .build();
    }

    private String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim();
    }
}