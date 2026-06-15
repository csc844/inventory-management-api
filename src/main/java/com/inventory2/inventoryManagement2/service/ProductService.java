package com.inventory2.inventoryManagement2.service;
import com.inventory2.inventoryManagement2.util.Constants;
import com.inventory2.inventoryManagement2.cache.RedisCacheService;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;
import com.inventory2.inventoryManagement2.repository.GenericRepository;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private static final String PRODUCT_KEY_PREFIX = "product:";

    private final GenericRepository repository;
    private final RedisCacheService redisCacheService;
    private final ObjectMapper objectMapper;

    // ---------------- CREATE — writes to Redis cache ----------------
    public void createProduct(Product product) {

        log.info("Creating product with name: {}", product.getName());

        validateSupplier(product);

        Supplier supplier = repository.findById(
                        Supplier.class,
                        product.getSupplier().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found with id: "
                                        + product.getSupplier().getId()));

        product.setSupplier(supplier);
        product.setCreatedAt(LocalDateTime.now());

        Product saved = repository.save(product);

        redisCacheService.put(
                Constants.PRODUCT_KEY_PREFIX + saved.getId(),
                saved
        );

        log.info("Product created with id: {}", saved.getId());
    }

    private void validateSupplier(Product product) {

        if (product.getSupplier() == null ||
                product.getSupplier().getId() == null) {

            throw new IllegalArgumentException(
                    "Supplier id is required");
        }
    }
}