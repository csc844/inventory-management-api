package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.dto.ProductRequestDto;
import com.inventory2.inventoryManagement2.dto.ProductResponseDto;
import com.inventory2.inventoryManagement2.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductController {

    private final ProductService productService;

    // CREATE PRODUCT
    @PostMapping
    public ProductResponseDto createProduct(@Valid @RequestBody ProductRequestDto dto) {
        return productService.createProduct(dto);
    }
}