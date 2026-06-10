package com.inventory2.inventoryManagement2.dto;

import lombok.Data;

@Data
public class StockRequestDto {

    private Long productId;

    private Integer quantity;

    private Integer minimumLevel;
}