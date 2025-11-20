package com.levelup.backend.dto.order;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {
    @NotBlank
    private String codigo;

    @NotBlank
    private String nombre;

    @Min(1)
    private int cantidad;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal precioUnitario;
}
