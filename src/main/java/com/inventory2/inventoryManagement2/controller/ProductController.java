package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.inventory2.inventoryManagement2.entity.Product;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductController {

    private final ProductService productService;

    // CREATE PRODUCT
    @PostMapping
    public ResponseEntity<String> createProduct(
            @Valid @RequestBody Product product) {

        productService.createProduct(product);

        return ResponseEntity.ok("Product created successfully");
    }
}