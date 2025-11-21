package com.levelup.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    @Column(name = "product_code", nullable = false, length = 64)
    private String productCode;

    @Column(name = "quantity", nullable = false)
    private int quantity;
}
