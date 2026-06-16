package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.entity.Stock;
import com.inventory2.inventoryManagement2.service.StockService;
import com.inventory2.inventoryManagement2.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/stock")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StockController {

    private final StockService stockService;

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addStock(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {

        stockService.addStock(productId, quantity);

        return ResponseUtil.success("Stock added successfully");
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