package com.inventory2.inventoryManagement2.entity;

import com.inventory2.inventoryManagement2.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_supplier", columnList = "supplier_id"),
                @Index(name = "idx_product_status",   columnList = "status"),
                @Index(name = "idx_product_name",     columnList = "name")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double price;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
}