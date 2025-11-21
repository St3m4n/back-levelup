package com.levelup.backend.dto.cart;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartDto {
    private final String userRun;
    private final List<CartItemDto> items;
    private final int totalQuantity;
    private final LocalDateTime updatedAt;
}
