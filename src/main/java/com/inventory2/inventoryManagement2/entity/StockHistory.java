package com.inventory2.inventoryManagement2.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;
    private String operation;        // ADD or REMOVE
    private Integer quantityChanged;
    private Integer quantityAfter;
    private LocalDateTime timestamp;
}