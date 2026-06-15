package com.inventory2.inventoryManagement2.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.inventory2.inventoryManagement2.cache.RedisCacheService;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Stock;
import com.inventory2.inventoryManagement2.exception.InsufficientStockException;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.kafka.KafkaProducerService;
import com.inventory2.inventoryManagement2.kafka.StockEvent;
import com.inventory2.inventoryManagement2.repository.GenericRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RedisCacheService redisCacheService;
    private final Cache<String, Stock> stockCaffeineCache;
    private final ObjectMapper objectMapper;

    // ---------------- ADD STOCK — writes to Redis + Caffeine, publishes Kafka event ----------------

    public void addStock(Long productId, Integer quantity) {
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
        redisCacheService.put(STOCK_KEY_PREFIX + productId, saved);
        stockCaffeineCache.put(STOCK_KEY_PREFIX + productId, saved);

        kafkaProducerService.publishStockEvent(StockEvent.builder()
                .productId(productId)
                .productName(product.getName())
                .operation("ADD")
                .quantityChanged(quantity)
                .quantityAfter(saved.getQuantity())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Stock updated for productId: {}, new quantity: {}", productId, saved.getQuantity());
    }

    // ---------------- REMOVE STOCK — writes to Redis + Caffeine, publishes Kafka event ----------------

    public Stock removeStock(Long productId, Integer quantity) {
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
        redisCacheService.put(STOCK_KEY_PREFIX + productId, saved);
        stockCaffeineCache.put(STOCK_KEY_PREFIX + productId, saved);


        kafkaProducerService.publishStockEvent(StockEvent.builder()
                .productId(productId)
                .productName(stock.getProduct().getName())
                .operation("REMOVE")
                .quantityChanged(quantity)
                .quantityAfter(saved.getQuantity())
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Stock removed for productId: {}, remaining: {}", productId, saved.getQuantity());
        return saved;
    }

    // ---------------- GET STOCK — Caffeine cache ----------------

    public Stock getStock(Long productId) {

        log.info("Fetching stock for productId: {}", productId);

        String key = STOCK_KEY_PREFIX + productId;

        // Level 1: Caffeine
        Stock caffeineStock = stockCaffeineCache.getIfPresent(key);

        if (caffeineStock != null) {
            log.debug("Caffeine cache hit for productId: {}", productId);
            return caffeineStock;
        }

        // Level 2: Redis
        Stock redisStock = redisCacheService.get(key, Stock.class);

        if (redisStock != null) {
            log.info("Redis cache hit for productId: {}", productId);

            stockCaffeineCache.put(key, redisStock);

            return redisStock;
        }

        // Level 3: Database
        log.info("Cache miss, querying DB for productId: {}", productId);

        Stock stock = repository.findByProperty(
                        Stock.class,
                        "FROM Stock s WHERE s.product.id = :productId",
                        "productId",
                        productId)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock not found for product id: " + productId));

        stockCaffeineCache.put(key, stock);

        redisCacheService.put(key, stock);

        return stock;
    }


}