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

    /**
     * When true, an empty items list is treated as a request to clear the cart.
     * When false (default), an empty list leaves the cart untouched unless clearCart endpoint is used.
     */
    private boolean forceReplace;
}
