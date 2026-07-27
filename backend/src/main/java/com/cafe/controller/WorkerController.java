package com.cafe.controller;

import com.cafe.dto.MenuItemResponse;
import com.cafe.dto.OrderResponse;
import com.cafe.dto.UpdateOrderStatusRequest;
import com.cafe.service.MenuService;
import com.cafe.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// WORKER (and ADMIN, via shared role check in SecurityConfig) endpoints
@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerController {

    private final OrderService orderService;
    private final MenuService menuService;

    // Live order queue - orders not yet completed/cancelled
    @GetMapping("/orders/queue")
    public ResponseEntity<List<OrderResponse>> getQueue() {
        return ResponseEntity.ok(orderService.getQueue());
    }

    // Worker claims a PLACED order (pessimistic lock prevents double-accept)
    @PutMapping("/orders/{id}/accept")
    public ResponseEntity<OrderResponse> accept(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(orderService.acceptOrder(auth.getName(), id));
    }

    // Move order through ACCEPTED -> PREPARING -> READY -> COMPLETED
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                        @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(orderService.updateStatus(id, request.getStatus()));
    }

    // Quick stock update without full menu-edit permissions
    @PutMapping("/menu/{id}/stock")
    public ResponseEntity<MenuItemResponse> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(menuService.updateStock(id, body.get("quantity")));
    }
}
