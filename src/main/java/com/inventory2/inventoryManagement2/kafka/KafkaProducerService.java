package com.inventory2.inventoryManagement2.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private static final String TOPIC = "stock-events";

    private final KafkaTemplate<String, StockEvent> kafkaTemplate;

    @Async
    public void publishStockEvent(StockEvent event) {

        try {
            log.info("Publishing Kafka event: {} for productId: {}",
                    event.getOperation(),
                    event.getProductId());

            kafkaTemplate.send(
                    TOPIC,
                    String.valueOf(event.getProductId()),
                    event
            ).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish Kafka event for productId: {}",
                            event.getProductId(),
                            ex);
                } else {
                    log.info("Kafka event published successfully for productId: {}",
                            event.getProductId());
                }
            });

        } catch (Exception e) {
            log.error("Unexpected error while publishing Kafka event for productId: {}",
                    event.getProductId(),
                    e);
        }
    }
}