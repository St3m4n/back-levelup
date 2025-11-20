package com.levelup.backend.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {
    @NotEmpty
    private List<@Valid OrderItemRequest> items;

    private String paymentMethod;

    @NotBlank
    private String direccion;

    private String region;

    private String comuna;
}
