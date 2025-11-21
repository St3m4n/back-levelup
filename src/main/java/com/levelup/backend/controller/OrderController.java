package com.levelup.backend.controller;

import com.levelup.backend.dto.order.CreateOrderRequest;
import com.levelup.backend.dto.order.OrderDto;
import com.levelup.backend.dto.order.UpdateOrderStatusRequest;
import com.levelup.backend.service.OrderService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> list(@RequestParam(required = false) String userEmail,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) String paymentMethod,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                       LocalDateTime from,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                       LocalDateTime to,
                                               @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(orderService.list(
                userEmail,
                status,
                paymentMethod,
                from,
                to,
                includeDeleted));
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateStatus(@PathVariable String orderId,
                                                 @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> delete(@PathVariable String orderId) {
        orderService.archive(orderId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderId}/restore")
    public ResponseEntity<OrderDto> restore(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.restore(orderId));
    }

}
