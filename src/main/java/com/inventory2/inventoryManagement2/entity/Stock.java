package com.inventory2.inventoryManagement2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stocks",
        indexes = {
                @Index(name = "idx_stock_product", columnList = "product_id", unique = true)
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer quantity;

    private Integer minimumLevel;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;
}