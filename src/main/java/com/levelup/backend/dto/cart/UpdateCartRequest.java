package com.levelup.backend.dto.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartRequest {
    @Valid
    @Size(max = 50)
    private List<CartItemRequest> items = new ArrayList<>();
}
