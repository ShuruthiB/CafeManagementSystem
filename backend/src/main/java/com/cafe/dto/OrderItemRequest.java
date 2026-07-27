package com.cafe.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull
    private Long menuItemId;

    @NotNull @Positive
    private Integer quantity;
}
