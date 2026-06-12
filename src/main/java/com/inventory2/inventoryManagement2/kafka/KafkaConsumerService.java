package com.inventory2.inventoryManagement2.kafka;

import com.inventory2.inventoryManagement2.entity.StockHistory;
import com.inventory2.inventoryManagement2.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final StockHistoryRepository stockHistoryRepository;

    @KafkaListener(topics = "stock-events", groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumeStockEvent(StockEvent event) {
        log.info("Consumed Kafka event: {} for productId: {}", event.getOperation(), event.getProductId());

        StockHistory history = StockHistory.builder()
                .productId(event.getProductId())
                .productName(event.getProductName())
                .operation(event.getOperation())
                .quantityChanged(event.getQuantityChanged())
                .quantityAfter(event.getQuantityAfter())
                .timestamp(event.getTimestamp())
                .build();

        stockHistoryRepository.save(history);
        log.info("Saved stock history record for productId: {}, operation: {}", event.getProductId(), event.getOperation());
    }
}