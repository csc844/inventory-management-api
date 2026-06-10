package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.dto.StockResponseDto;
import com.inventory2.inventoryManagement2.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StockController {

    private final StockService stockService;

    // ADD STOCK
    @PostMapping("/add")
    public StockResponseDto addStock(@Valid @RequestParam Long productId,
                                     @Valid @RequestParam Integer quantity) {
        return stockService.addStock(productId, quantity);
    }

    // REMOVE STOCK
    @PostMapping("/remove")
    public StockResponseDto removeStock(@Valid @RequestParam Long productId,
                                        @Valid @RequestParam Integer quantity) {
        return stockService.removeStock(productId, quantity);
    }

    // GET STOCK
    @GetMapping("/{productId}")
    public StockResponseDto getStock(@Valid @PathVariable Long productId) {
        return stockService.getStock(productId);
    }
}