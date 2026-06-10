package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.dto.StockResponseDto;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Stock;
import com.inventory2.inventoryManagement2.repository.ProductRepository;
import com.inventory2.inventoryManagement2.repository.StockRepository;
import com.inventory2.inventoryManagement2.exception.InsufficientStockException;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StockService   {

    private final StockRepository stockRepository;
    private final ProductRepository productRepository;


    // ---------------- ADD STOCK ----------------

    public StockResponseDto addStock(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Stock stock = stockRepository.findById(productId).orElse(null);

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

        Stock saved = stockRepository.save(stock);

        return mapToDto(saved);
    }

    // ---------------- REMOVE STOCK ----------------
    public StockResponseDto removeStock(Long productId, Integer quantity) {

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product id: " + productId));

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock: available " + stock.getQuantity() + ", requested " + quantity);
        }

        stock.setQuantity(stock.getQuantity() - quantity);

        Stock saved = stockRepository.save(stock);

        return mapToDto(saved);
    }

    // ---------------- GET STOCK ----------------

    public StockResponseDto getStock(Long productId) {

        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found for product id: " + productId));

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

        // STATUS LOGIC (VERY IMPORTANT)
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