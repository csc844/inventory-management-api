package com.inventory2.inventoryManagement2.cache;

import com.inventory2.inventoryManagement2.util.Constants;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void put(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(value),
                    Duration.ofMinutes(Constants.REDIS_TTL_MINUTES)
            );

            log.debug("Saved to Redis: {}", key);

        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }

    public <T> T get(String key, Class<T> clazz) {
        try {
            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json, clazz);

        } catch (Exception e) {
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);

            log.debug("Removed from Redis: {}", key);

        } catch (Exception e) {
            log.warn("Redis delete failed for key {}: {}", key, e.getMessage());
        }
    }
}