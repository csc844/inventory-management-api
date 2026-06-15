package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // ---------------- CREATE — writes to Redis cache ----------------
    public Product createProduct(Product product) {

        log.info("Creating product with name: {}", product.getName());

        if (product.getSupplier() == null || product.getSupplier().getId() == null) {
            throw new IllegalArgumentException("Supplier id is required");
        }

        Long supplierId = product.getSupplier().getId();

        Supplier supplier = repository.findById(Supplier.class, supplierId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found with id: " + supplierId));
        product.setSupplier(supplier);
        product.setCreatedAt(LocalDateTime.now());

        Product saved = repository.save(product);

        putToRedis(PRODUCT_KEY_PREFIX + saved.getId(), saved);

        log.info("Product created with id: {}", saved.getId());

        return saved;
    }

    // ---------------- Redis helper ----------------

    private void putToRedis(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), Duration.ofMinutes(30));
            log.info("Saved to Redis: {}", key);
        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }
}