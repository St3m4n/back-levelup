package com.levelup.backend.service;

import com.levelup.backend.dto.order.CreateOrderRequest;
import com.levelup.backend.dto.order.OrderDto;
import com.levelup.backend.dto.order.OrderItemDto;
import com.levelup.backend.dto.order.UpdateOrderStatusRequest;
import com.levelup.backend.model.Order;
import com.levelup.backend.model.OrderItem;
import com.levelup.backend.model.OrderStatus;
import com.levelup.backend.model.PaymentMethod;
import com.levelup.backend.model.Usuario;
import com.levelup.backend.model.UsuarioPerfil;
import com.levelup.backend.repository.OrderRepository;
import com.levelup.backend.repository.UsuarioRepository;
import com.levelup.backend.security.LevelUpUserDetails;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<OrderDto> list(String userEmail, String status, String paymentMethod,
                               LocalDateTime from, LocalDateTime to, boolean includeDeleted) {
        LevelUpUserDetails principal = requirePrincipal();
        boolean admin = isAdmin(principal);
        String normalizedEmail = admin ? normalizeEmail(userEmail) : principal.getUser().getCorreo().toLowerCase(Locale.ROOT);
        Specification<Order> spec = safeCombine(null, includeDeletedSpec(includeDeleted));
        spec = safeCombine(spec, statusSpec(status));
        spec = safeCombine(spec, paymentMethodSpec(paymentMethod));
        spec = safeCombine(spec, userEmailSpec(normalizedEmail));
        spec = safeCombine(spec, dateRangeSpec(from, to));
        List<Order> orders = spec == null
                ? orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                : orderRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public OrderDto create(CreateOrderRequest request) {
        LevelUpUserDetails principal = getPrincipal();
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        Usuario usuario = usuarioRepository.findByRun(principal.getUser().getRun())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        if (request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requieren ítems en la orden");
        }
        BigDecimal total = request.getItems().stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .usuario(usuario)
                .userEmail(principal.getUser().getCorreo().toLowerCase(Locale.ROOT))
                .userName(principal.getUser().getNombre())
                .total(total)
                .paymentMethod(parsePaymentMethod(request.getPaymentMethod()))
                .status(OrderStatus.Pendiente)
                .direccion(request.getDireccion().trim())
                .region(trimToNull(request.getRegion()))
                .comuna(trimToNull(request.getComuna()))
                .build();
        List<OrderItem> items = request.getItems().stream()
                .map(item -> OrderItem.builder()
                        .order(order)
                        .codigo(item.getCodigo().trim())
                        .nombre(item.getNombre().trim())
                        .cantidad(item.getCantidad())
                        .precioUnitario(item.getPrecioUnitario())
                        .subtotal(item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                        .build())
                .collect(Collectors.toList());
        order.setItems(items);
        Order persisted = orderRepository.save(order);
        return toDto(persisted);
    }

    @Transactional
    public OrderDto updateStatus(String orderId, UpdateOrderStatusRequest request) {
        LevelUpUserDetails principal = getPrincipal();
        if (!isAdmin(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden cambiar el estado");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        OrderStatus nextStatus = parseStatus(request.getStatus());
        order.setStatus(nextStatus);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public void archive(String orderId) {
        LevelUpUserDetails principal = getPrincipal();
        if (!isAdmin(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden eliminar órdenes");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        if (order.getDeletedAt() == null) {
            order.setDeletedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }

    @Transactional
    public OrderDto restore(String orderId) {
        LevelUpUserDetails principal = getPrincipal();
        if (!isAdmin(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo administradores pueden restaurar órdenes");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
        if (order.getDeletedAt() != null) {
            order.setDeletedAt(null);
            order = orderRepository.save(order);
        }
        return toDto(order);
    }

    private Specification<Order> includeDeletedSpec(boolean includeDeleted) {
        if (includeDeleted) {
            return null;
        }
        return (root, query, builder) -> builder.isNull(root.get("deletedAt"));
    }

    private Specification<Order> statusSpec(String statusValue) {
        OrderStatus status = tryParseStatus(statusValue);
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    private Specification<Order> paymentMethodSpec(String methodValue) {
        PaymentMethod method = tryParsePaymentMethod(methodValue);
        if (method == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("paymentMethod"), method);
    }

    private Specification<Order> userEmailSpec(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return null;
        }
        return (root, query, builder) -> builder.equal(builder.lower(root.get("userEmail")), userEmail);
    }

    private Specification<Order> dateRangeSpec(LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) {
            return null;
        }
        return (root, query, builder) -> {
            if (from != null && to != null) {
                return builder.between(root.get("createdAt"), from, to);
            }
            if (from != null) {
                return builder.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return builder.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    private Specification<Order> safeCombine(Specification<Order> base, Specification<Order> addition) {
        if (base == null) {
            return addition;
        }
        return addition == null ? base : base.and(addition);
    }

    private OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        return OrderDto.builder()
                .id(order.getId())
                .userEmail(order.getUserEmail())
                .userName(order.getUserName())
                .total(order.getTotal())
                .paymentMethod(order.getPaymentMethod())
                .status(order.getStatus())
                .direccion(order.getDireccion())
                .region(order.getRegion())
                .comuna(order.getComuna())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .deletedAt(order.getDeletedAt())
                .items(items)
                .build();
    }

    private OrderItemDto toItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .codigo(item.getCodigo())
                .nombre(item.getNombre())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build();
    }

    private PaymentMethod parsePaymentMethod(String value) {
        PaymentMethod method = tryParsePaymentMethod(value);
        return method == null ? PaymentMethod.tarjeta : method;
    }

    private OrderStatus parseStatus(String value) {
        OrderStatus status = tryParseStatus(value);
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }
        return status;
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OrderStatus tryParseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (OrderStatus candidate : OrderStatus.values()) {
            if (candidate.name().equalsIgnoreCase(normalized)) {
                return candidate;
            }
        }
        return null;
    }

    private PaymentMethod tryParsePaymentMethod(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (PaymentMethod candidate : PaymentMethod.values()) {
            if (candidate.name().equalsIgnoreCase(normalized)) {
                return candidate;
            }
        }
        return null;
    }

    private LevelUpUserDetails getPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof LevelUpUserDetails details ? details : null;
    }

    private LevelUpUserDetails requirePrincipal() {
        LevelUpUserDetails principal = getPrincipal();
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return principal;
    }

    private boolean isAdmin(LevelUpUserDetails principal) {
        return principal != null && isAdminProfile(principal.getUser().getPerfil());
    }

    private boolean isAdminProfile(UsuarioPerfil perfil) {
        return perfil == UsuarioPerfil.Administrador || perfil == UsuarioPerfil.Vendedor;
    }
}
