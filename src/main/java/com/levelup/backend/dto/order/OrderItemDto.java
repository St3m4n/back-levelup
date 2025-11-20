package com.levelup.backend.dto.order;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemDto {
    private final String codigo;
    private final String nombre;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;
}
