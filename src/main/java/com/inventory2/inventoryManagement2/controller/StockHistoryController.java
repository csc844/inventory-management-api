package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.entity.StockHistory;
import com.inventory2.inventoryManagement2.service.StockHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    // GET ALL HISTORY
    @GetMapping
    public List<StockHistory> getAllHistory() {
        return stockHistoryService.getAllHistory();
    }

    // GET BY PRODUCT ID
    @GetMapping("/product/{productId}")
    public List<StockHistory> getByProductId(@PathVariable Long productId) {
        return stockHistoryService.getHistoryByProductId(productId);
    }
}