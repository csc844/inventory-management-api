package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.dto.ProductRequestDto;
import com.inventory2.inventoryManagement2.dto.ProductResponseDto;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.repository.ProductRepository;
import com.inventory2.inventoryManagement2.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private static final String PRODUCT_KEY_PREFIX = "product:";

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // ---------------- CREATE — writes to Redis cache ----------------

    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating product with name: {} for supplierId: {}", dto.getName(), dto.getSupplierId());

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));

        Product product = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .supplier(supplier)
                .status(null)
                .createdAt(LocalDateTime.now())
                .build();

        Product saved = productRepository.save(product);

        ProductResponseDto response = new ProductResponseDto();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setPrice(saved.getPrice());
        response.setSupplierName(supplier.getName());
        response.setStatus(String.valueOf(saved.getStatus()));
        response.setCreatedAt(saved.getCreatedAt());

        putToRedis(PRODUCT_KEY_PREFIX + saved.getId(), response);

        log.info("Product created with id: {}", saved.getId());
        return response;
    }

    // ---------------- Redis helper ----------------

    private void putToRedis(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), Duration.ofMinutes(30));
            log.debug("Saved to Redis: {}", key);
        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }
}