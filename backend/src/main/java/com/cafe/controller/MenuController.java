package com.cafe.controller;

import com.cafe.dto.MenuItemRequest;
import com.cafe.dto.MenuItemResponse;
import com.cafe.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    // Public: anyone (even not logged in) can browse the menu
    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenu() {
        return ResponseEntity.ok(menuService.getAllAvailable());
    }

    // Staff view: includes out-of-stock/unavailable items
    @GetMapping("/staff-view")
    @PreAuthorize("hasAnyRole('WORKER','ADMIN')")
    public ResponseEntity<List<MenuItemResponse>> getMenuForStaff() {
        return ResponseEntity.ok(menuService.getAllForStaff());
    }

    // Admin only: full item creation
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> create(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.create(request));
    }

    // Admin only: full edit (price, description, category, etc.)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuItemResponse> update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuService.update(id, request));
    }

    // Admin only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
