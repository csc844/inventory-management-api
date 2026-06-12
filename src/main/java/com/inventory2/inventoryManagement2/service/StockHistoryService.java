package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.entity.StockHistory;
import com.inventory2.inventoryManagement2.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;

    public List<StockHistory> getAllHistory() {
        return stockHistoryRepository.findAll();
    }

    public List<StockHistory> getHistoryByProductId(Long productId) {
        return stockHistoryRepository.findByProductId(productId);
    }
}