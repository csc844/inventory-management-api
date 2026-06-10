package com.inventory2.inventoryManagement2.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "suppliers",
        indexes = {
                @Index(name = "idx_supplier_email", columnList = "email", unique = true),
                @Index(name = "idx_supplier_name",  columnList = "name")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String email;

    private String phone;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "supplier")
    private List<Product> products;
}