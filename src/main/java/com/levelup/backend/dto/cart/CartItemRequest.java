package com.levelup.backend.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequest {
    @NotBlank
    private String productCode;

    @Min(1)
    private int quantity;
}
