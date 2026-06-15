package com.inventory2.inventoryManagement2.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inventory2.inventoryManagement2.dto.StockResponseDto;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Stock;
import com.inventory2.inventoryManagement2.exception.InsufficientStockException;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.kafka.KafkaProducerService;
import com.inventory2.inventoryManagement2.kafka.StockEvent;
import com.inventory2.inventoryManagement2.repository.GenericRepository;
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
public class StockService {

    private static final String STOCK_KEY_PREFIX = "stock:";

    private final GenericRepository repository;
    private final KafkaProducerService kafkaProducerService;
    private final StringRedisTemplate redisTemplate;
    private final Cache<String, StockResponseDto> stockCaffeineCache;
    private final ObjectMapper objectMapper;

    // ---------------- ADD STOCK — writes to Redis + Caffeine, publishes Kafka event ----------------

    public StockResponseDto addStock(Long productId, Integer quantity) {
        log.info("Adding {} units to productId: {}", quantity, productId);

        Product product = repository
                .findById(Product.class, productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Product not found with id: " + productId));

        Stock stock = repository.findByProperty(
                        Stock.class,
                        "FROM Stock s WHERE s.product.id = :productId",
                        "productId",
                        productId
                ).stream()
                .findFirst()
                .orElse(null);
        if (stock == null) {
            stock = Stock.builder()
                    .product(product)
                    .quantity(quantity)
                    .minimumLevel(10)
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            stock.setQuantity(stock.getQuantity() + quantity);
        }

        Stock saved = repository.save(stock);
        StockResponseDto response = mapToDto(saved);

        putToRedis(STOCK_KEY_PREFIX + productId, response);
        stockCaffeineCache.put(STOCK_KEY_PREFIX + productId, response);

        kafkaProducerService.publishStockEvent(StockEvent.builder()
                .productId(productId)
                .productName(product.getName())
                .operation("ADD")
                .quantityChanged(quantity)
                .quantityAfter(saved.getQuantity())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Stock updated for productId: {}, new quantity: {}", productId, saved.getQuantity());
        return response;
    }

    // ---------------- REMOVE STOCK — writes to Redis + Caffeine, publishes Kafka event ----------------

    public StockResponseDto removeStock(Long productId, Integer quantity) {
        log.info("Removing {} units from productId: {}", quantity, productId);

        Stock stock = repository.findByProperty(
                        Stock.class,
                        "FROM Stock s WHERE s.product.id = :productId",
                        "productId",
                        productId
                ).stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock not found for product id: " + productId));

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock: available " + stock.getQuantity() + ", requested " + quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        Stock saved = repository.save(stock);
        StockResponseDto response = mapToDto(saved);

        putToRedis(STOCK_KEY_PREFIX + productId, response);
        stockCaffeineCache.put(STOCK_KEY_PREFIX + productId, response);

        kafkaProducerService.publishStockEvent(StockEvent.builder()
                .productId(productId)
                .productName(stock.getProduct().getName())
                .operation("REMOVE")
                .quantityChanged(quantity)
                .quantityAfter(saved.getQuantity())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Stock removed for productId: {}, remaining: {}", productId, saved.getQuantity());
        return response;
    }

    // ---------------- GET STOCK — Caffeine cache ----------------

    public StockResponseDto getStock(Long productId) {
        log.info("Fetching stock for productId: {}", productId);
        String key = STOCK_KEY_PREFIX + productId;

        StockResponseDto cached = stockCaffeineCache.getIfPresent(key);
        if (cached != null) {
            log.debug("Caffeine cache hit for productId: {}", productId);
            return cached;
        }

        log.debug("Caffeine cache miss, querying DB for productId: {}", productId);
        Stock stock = repository.findByProperty(
                        Stock.class,
                        "FROM Stock s WHERE s.product.id = :productId",
                        "productId",
                        productId
                ).stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock not found for product id: " + productId));

        StockResponseDto response = mapToDto(stock);
        stockCaffeineCache.put(key, response);
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

    // ---------------- Mapper ----------------

    private StockResponseDto mapToDto(Stock stock) {
        StockResponseDto dto = new StockResponseDto();
        dto.setId(stock.getId());
        dto.setProductName(stock.getProduct().getName());
        dto.setQuantity(stock.getQuantity());
        dto.setMinimumLevel(stock.getMinimumLevel());
        dto.setCreatedAt(stock.getCreatedAt());

        if (stock.getQuantity() <= stock.getMinimumLevel()) {
            dto.setStatus("REORDER");
        } else if (stock.getQuantity() <= stock.getMinimumLevel() * 2) {
            dto.setStatus("LOW");
        } else {
            dto.setStatus("OK");
        }
        return dto;
    }
}