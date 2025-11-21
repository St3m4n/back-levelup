package com.levelup.backend.service;

import com.levelup.backend.dto.cart.CartDto;
import com.levelup.backend.dto.cart.CartItemDto;
import com.levelup.backend.dto.cart.CartItemRequest;
import com.levelup.backend.dto.cart.UpdateCartRequest;
import com.levelup.backend.model.Cart;
import com.levelup.backend.model.CartItem;
import com.levelup.backend.model.Producto;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.repository.CartRepository;
import com.levelup.backend.repository.ProductoRepository;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.LevelUpUserDetails;
import com.levelup.backend.util.SecurityUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CartService {
    private static final int MAX_TOTAL_QUANTITY = 50;

    private final CartRepository cartRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public CartDto getMyCart() {
        Usuario usuario = getCurrentUser();
        return cartRepository.findByUsuarioRun(usuario.getRun())
            .map(this::toDto)
            .orElseGet(() -> CartDto.builder()
                .userRun(usuario.getRun())
                .items(List.of())
                .totalQuantity(0)
                .updatedAt(null)
                .build());
    }

    @Transactional
    public CartDto replaceCart(UpdateCartRequest request) {
        Usuario usuario = getCurrentUser();
        List<CartItemRequest> requestedItems = request == null ? null : request.getItems();
        boolean forceReplace = request != null && request.isForceReplace();

        Cart existingCart = cartRepository.findByUsuarioRun(usuario.getRun()).orElse(null);

        if (requestedItems == null || requestedItems.isEmpty()) {
            if (!forceReplace) {
                if (existingCart == null) {
                    return CartDto.builder()
                            .userRun(usuario.getRun())
                            .items(List.of())
                            .totalQuantity(0)
                            .updatedAt(null)
                            .build();
                }
                return toDto(existingCart);
            }
        }

        List<CartItem> validatedItems = validateAndMapItems(requestedItems == null ? List.of() : requestedItems);

        Cart cart = existingCart != null
                ? existingCart
                : Cart.builder()
                        .usuario(usuario)
                        .items(new ArrayList<>())
                        .build();
        if (cart.getId() == null) {
            cart.setItems(new ArrayList<>(validatedItems));
        } else {
            cart.getItems().clear();
            cart.getItems().addAll(validatedItems);
        }
        Cart saved = cartRepository.save(cart);
        return toDto(saved);
    }

    @Transactional
    public void clearCart() {
        Usuario usuario = getCurrentUser();
        cartRepository.findByUsuarioRun(usuario.getRun()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    private List<CartItem> validateAndMapItems(List<CartItemRequest> requestedItems) {
        List<CartItemRequest> items = requestedItems == null ? List.of() : requestedItems;
        List<CartItem> result = new ArrayList<>(items.size());
        Set<String> seen = new HashSet<>();
        int totalQuantity = 0;

        for (CartItemRequest item : items) {
            String normalizedCode = normalizeCode(item.getProductCode());
            if (!seen.add(normalizedCode)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Producto duplicado en el carrito: " + normalizedCode);
            }
            Producto producto = productoRepository.findByCodigo(normalizedCode)
                    .filter(p -> p.getDeletedAt() == null)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Producto inválido: " + normalizedCode));
            int quantity = item.getQuantity();
            if (producto.getStock() < quantity) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Stock insuficiente para el producto " + normalizedCode);
            }
            totalQuantity += quantity;
            if (totalQuantity > MAX_TOTAL_QUANTITY) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El carrito no puede exceder " + MAX_TOTAL_QUANTITY + " unidades en total");
            }
            result.add(CartItem.builder()
                    .productCode(producto.getCodigo())
                    .quantity(quantity)
                    .build());
        }
        return result;
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> items = cart.getItems().stream()
                .map(item -> CartItemDto.builder()
                        .productCode(item.getProductCode())
                        .quantity(item.getQuantity())
                        .build())
                .toList();
        int totalQuantity = items.stream().mapToInt(CartItemDto::getQuantity).sum();
        return CartDto.builder()
                .userRun(cart.getUsuario().getRun())
                .items(items)
                .totalQuantity(totalQuantity)
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de producto inválido");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private Usuario getCurrentUser() {
        LevelUpUserDetails principal = SecurityUtils.getCurrentUserDetails()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
        return usuarioRepository.findByRun(principal.getUser().getRun())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inexistente"));
    }
}
