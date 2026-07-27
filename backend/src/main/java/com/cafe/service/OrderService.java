package com.cafe.service;

import com.cafe.dto.*;
import com.cafe.entity.*;
import com.cafe.repository.MenuItemRepository;
import com.cafe.repository.OrderRepository;
import com.cafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    // ---------- USER ----------

    @Transactional
    public OrderResponse placeOrder(String userEmail, OrderRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + itemReq.getMenuItemId()));

            if (!menuItem.isAvailable() || menuItem.getStockQuantity() < itemReq.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for: " + menuItem.getName());
            }

            // decrement stock at order time
            menuItem.setStockQuantity(menuItem.getStockQuantity() - itemReq.getQuantity());
            if (menuItem.getStockQuantity() == 0) {
                menuItem.setAvailable(false);
            }
            menuItemRepository.save(menuItem);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .priceAtOrderTime(menuItem.getPrice())
                    .build();

            order.getItems().add(orderItem);
            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public List<OrderResponse> getMyOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return orderRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse cancelOrder(String requesterEmail, Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order can no longer be cancelled - already " + order.getStatus());
        }

        // restock items since the order didn't go through
        for (OrderItem item : order.getItems()) {
            MenuItem menuItem = item.getMenuItem();
            menuItem.setStockQuantity(menuItem.getStockQuantity() + item.getQuantity());
            menuItem.setAvailable(true);
            menuItemRepository.save(menuItem);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    // ---------- WORKER / ADMIN ----------

    // Live queue: everything not yet completed or cancelled
    public List<OrderResponse> getQueue() {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(
                        List.of(OrderStatus.PLACED, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Worker "claims" an order - pessimistic lock stops two workers
    // from accepting the same order at the same time.
    @Transactional
    public OrderResponse acceptOrder(String workerEmail, Long orderId) {
        User worker = userRepository.findByEmail(workerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found"));

        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Order already " + order.getStatus() + " - cannot accept again");
        }

        order.setStatus(OrderStatus.ACCEPTED);
        order.setHandledBy(worker);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        validateTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case ACCEPTED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.READY;
            case READY -> next == OrderStatus.COMPLETED;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Cannot move order from " + current + " to " + next);
        }
    }

    // ---------- ADMIN ----------

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getMenuItem().getName(), i.getQuantity(), i.getPriceAtOrderTime()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getName(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getHandledBy() != null ? order.getHandledBy().getName() : null,
                items
        );
    }
}
