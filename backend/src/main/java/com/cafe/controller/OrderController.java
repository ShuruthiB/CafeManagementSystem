package com.cafe.controller;

import com.cafe.dto.OrderRequest;
import com.cafe.dto.OrderResponse;
import com.cafe.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// USER-facing order endpoints
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(Authentication auth, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(auth.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(auth.getName(), id));
    }
}
