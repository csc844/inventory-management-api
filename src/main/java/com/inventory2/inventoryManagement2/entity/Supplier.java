package com.inventory2.inventoryManagement2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(
        name = "suppliers",
        indexes = {
                @Index(name = "idx_supplier_email", columnList = "email", unique = true),
                @Index(name = "idx_supplier_name", columnList = "name")
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

    @NotBlank(message = "Supplier name is required")
    @Size(
            min = 2,
            max = 50,
            message = "Supplier name must be between 2 and 50 characters"
    )
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Phone number must contain 10 digits"
    )
    private String phone;

    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "supplier")
    private List<Product> products;
}