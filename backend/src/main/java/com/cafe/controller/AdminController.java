package com.cafe.controller;

import com.cafe.dto.CreateStaffRequest;
import com.cafe.entity.User;
import com.cafe.service.OrderService;
import com.cafe.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ADMIN-only endpoints: staff management + reporting
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final OrderService orderService;

    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        User staff = userService.createStaff(request);
        return ResponseEntity.ok(Map.of(
                "id", staff.getId(), "name", staff.getName(),
                "email", staff.getEmail(), "role", staff.getRole()
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        userService.setEnabled(id, false);
        return ResponseEntity.ok(Map.of("message", "User disabled"));
    }

    @PutMapping("/users/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        userService.setEnabled(id, true);
        return ResponseEntity.ok(Map.of("message", "User enabled"));
    }

    // Simple sales/order report - full order history across all users
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }
}
