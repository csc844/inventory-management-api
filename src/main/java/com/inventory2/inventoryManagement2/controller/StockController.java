package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.entity.Stock;
import com.inventory2.inventoryManagement2.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StockController {

    private final StockService stockService;

    @PostMapping("/add")
    public ResponseEntity<String> addStock(@RequestParam Long productId,
                                           @RequestParam Integer quantity) {

        stockService.addStock(productId, quantity);

        return ResponseEntity.ok("Stock added successfully");
    }

    @PostMapping("/remove")
    public Stock removeStock(@RequestParam Long productId,
                             @RequestParam Integer quantity) {

        return stockService.removeStock(productId, quantity);
    }

    @GetMapping("/{productId}")
    public Stock getStock(@PathVariable Long productId) {

        return stockService.getStock(productId);
    }
}