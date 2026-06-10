package com.inventory2.inventoryManagement2.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductResponseDto {

    private Long id;

    private String name;

    private Double price;

    private String supplierName;

    private String status;

    private LocalDateTime createdAt;
}