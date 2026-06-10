package com.inventory2.inventoryManagement2.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierResponseDto {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private int totalProducts;

    private LocalDateTime createdAt;
}