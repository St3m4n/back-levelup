package com.levelup.backend.controller;

import com.levelup.backend.dto.cart.CartDto;
import com.levelup.backend.dto.cart.UpdateCartRequest;
import com.levelup.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/me")
    public ResponseEntity<CartDto> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PutMapping("/me")
    public ResponseEntity<CartDto> replaceMyCart(@Valid @RequestBody UpdateCartRequest request) {
        return ResponseEntity.ok(cartService.replaceCart(request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> clearMyCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
