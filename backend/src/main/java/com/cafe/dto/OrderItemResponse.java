package com.cafe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemResponse {
    private String menuItemName;
    private Integer quantity;
    private BigDecimal priceAtOrderTime;
}
