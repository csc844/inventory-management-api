package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.entity.StockHistory;
import com.inventory2.inventoryManagement2.repository.GenericRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StockHistoryService {

    private final GenericRepository repository;

    public List<StockHistory> getAllHistory() {
        return repository.findAll(StockHistory.class);
    }

    public List<StockHistory> getHistoryByProductId(Long productId) {

        return repository.findByProperty(
                StockHistory.class,
                "FROM StockHistory WHERE productId = :productId ORDER BY id DESC",
                "productId",
                productId
        );
    }
}