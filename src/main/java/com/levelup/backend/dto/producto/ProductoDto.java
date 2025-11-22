package com.levelup.backend.dto.producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductoDto {
    private final String codigo;
    private final String nombre;
    private final String descripcion;
    private final String categoria;
    private final String fabricante;
    private final String distribuidor;
    private final BigDecimal precio;
    private final int stock;
    private final int stockCritico;
    private final String imagenUrl;
    private final boolean eliminado;
    private final LocalDateTime deletedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}