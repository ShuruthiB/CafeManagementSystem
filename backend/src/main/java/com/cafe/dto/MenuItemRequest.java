package com.cafe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuItemRequest {
    @NotBlank
    private String name;

    private String description;

    @NotNull @Positive
    private BigDecimal price;

    @NotBlank
    private String category;

    @NotNull @PositiveOrZero
    private Integer stockQuantity;
}
