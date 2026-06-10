package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.dto.StockResponseDto;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Stock;
import com.inventory2.inventoryManagement2.exception.InsufficientStockException;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.repository.ProductRepository;
import com.inventory2.inventoryManagement2.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StockService   {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;

    // ---------------- ADD STOCK ----------------

    public StockResponseDto addStock(Long productId, Integer quantity) {
        log.info("Adding {} units to productId: {}", quantity, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.warn("Product not found with id: {}", productId);
                    return new ResourceNotFoundException("Product not found with id: " + productId);
                });

        Stock stock = stockRepository.findById(productId).orElse(null);

        if (stock == null) {
            log.debug("No existing stock for productId: {}, creating new stock entry", productId);
            stock = Stock.builder()
                    .product(product)
                    .quantity(quantity)
                    .minimumLevel(10)
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            log.debug("Existing stock: {}, adding: {}", stock.getQuantity(), quantity);
            stock.setQuantity(stock.getQuantity() + quantity);
        }

        Stock saved = stockRepository.save(stock);
        log.info("Stock updated for productId: {}, new quantity: {}", productId, saved.getQuantity());

        return mapToDto(saved);
    }

    // ---------------- REMOVE STOCK ----------------

    public StockResponseDto removeStock(Long productId, Integer quantity) {
        log.info("Removing {} units from productId: {}", quantity, productId);

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("Stock not found for productId: {}", productId);
                    return new ResourceNotFoundException("Stock not found for product id: " + productId);
                });

        if (stock.getQuantity() < quantity) {
            log.warn("Insufficient stock for productId: {}. Available: {}, Requested: {}", productId, stock.getQuantity(), quantity);
            throw new InsufficientStockException("Insufficient stock: available " + stock.getQuantity() + ", requested " + quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        Stock saved = stockRepository.save(stock);
        log.info("Stock removed for productId: {}, remaining quantity: {}", productId, saved.getQuantity());

        return mapToDto(saved);
    }

    // ---------------- GET STOCK ----------------

    public StockResponseDto getStock(Long productId) {
        log.info("Fetching stock for productId: {}", productId);

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    log.warn("Stock not found for productId: {}", productId);
                    return new ResourceNotFoundException("Stock not found for product id: " + productId);
                });

        return mapToDto(stock);
    }

    // ---------------- MAPPER ----------------
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

        log.debug("Stock status for product '{}': {}", stock.getProduct().getName(), dto.getStatus());
        return dto;
    }
}