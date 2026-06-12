package com.inventory2.inventoryManagement2.kafka;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockEvent implements Serializable {

    private Long productId;
    private String productName;
    private String operation;        // ADD or REMOVE
    private Integer quantityChanged;
    private Integer quantityAfter;
    private LocalDateTime timestamp;
}