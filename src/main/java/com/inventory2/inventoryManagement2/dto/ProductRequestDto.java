package com.inventory2.inventoryManagement2.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotNull
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "SupplierId is required")
    private Long supplierId;
}