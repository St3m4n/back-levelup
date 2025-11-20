package com.levelup.backend.dto.producto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProductoRequest {
    private String codigo;

    @NotBlank
    private String nombre;

    private String descripcion;

    @NotBlank
    private String categoria;

    private String fabricante;

    private String distribuidor;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal precio;

    @Min(0)
    private int stock;

    @Min(0)
    private int stockCritico;

    private String imagenUrl;
}