package com.inventory2.inventoryManagement2.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockResponseDto {

    private Long id;

    private String productName;

    private Integer quantity;

    private Integer minimumLevel;

    private String status;

    private LocalDateTime createdAt;
}