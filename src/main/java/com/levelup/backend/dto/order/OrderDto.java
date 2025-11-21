package com.levelup.backend.dto.order;

import com.levelup.backend.model.OrderStatus;
import com.levelup.backend.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderDto {
    private final String id;
    private final String userEmail;
    private final String userName;
    private final BigDecimal total;
    private final PaymentMethod paymentMethod;
    private final OrderStatus status;
    private final String direccion;
    private final String region;
    private final String comuna;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final LocalDateTime deletedAt;
    private final List<OrderItemDto> items;
}
