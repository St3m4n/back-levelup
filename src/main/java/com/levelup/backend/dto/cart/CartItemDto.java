package com.levelup.backend.dto.cart;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemDto {
    private final String productCode;
    private final int quantity;
}
