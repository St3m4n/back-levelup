package com.levelup.backend.dto.producto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoriaDto {
    private final Long id;
    private final String nombre;
    private final boolean eliminada;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}