package com.cafe.repository;

import com.cafe.entity.Order;
import com.cafe.entity.OrderStatus;
import com.cafe.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    // Pessimistic write lock so two workers can't accept the same order
    // at the same time - pairs with the producer-consumer queue pattern.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);
}
