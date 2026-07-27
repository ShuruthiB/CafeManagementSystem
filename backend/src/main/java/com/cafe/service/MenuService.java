package com.cafe.service;

import com.cafe.dto.MenuItemRequest;
import com.cafe.dto.MenuItemResponse;
import com.cafe.entity.MenuItem;
import com.cafe.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    public List<MenuItemResponse> getAllAvailable() {
        return menuItemRepository.findByAvailableTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    // Admin/Worker view includes unavailable/out-of-stock items too
    public List<MenuItemResponse> getAllForStaff() {
        return menuItemRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public MenuItemResponse create(MenuItemRequest request) {
        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .stockQuantity(request.getStockQuantity())
                .available(request.getStockQuantity() > 0)
                .build();

        return toResponse(menuItemRepository.save(item));
    }

    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));

        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setStockQuantity(request.getStockQuantity());
        item.setAvailable(request.getStockQuantity() > 0);

        return toResponse(menuItemRepository.save(item));
    }

    // Worker-facing: quick stock adjustment without touching price/description
    public MenuItemResponse updateStock(Long id, int newQuantity) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + id));

        item.setStockQuantity(newQuantity);
        item.setAvailable(newQuantity > 0);

        return toResponse(menuItemRepository.save(item));
    }

    public void delete(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new IllegalArgumentException("Menu item not found: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(), item.getName(), item.getDescription(),
                item.getPrice(), item.getCategory(), item.getStockQuantity(), item.isAvailable()
        );
    }
}
