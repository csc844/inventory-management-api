package com.inventory2.inventoryManagement2.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.inventory2.inventoryManagement2.entity.Stock;
import com.inventory2.inventoryManagement2.util.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CaffeineConfig {

    @Bean
    public Cache<String, Stock> stockCaffeineCache() {

        return Caffeine.newBuilder()
                .expireAfterWrite(
                        Duration.ofMinutes(
                                Constants.CAFFEINE_EXPIRE_MINUTES))
                .maximumSize(
                        Constants.CAFFEINE_MAX_SIZE)
                .build();
    }
}