package com.inventory2.inventoryManagement2.cache;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LruCacheService {

    private static final int MAX_SIZE = 100;

    private final Map<String, Object> cache =
            Collections.synchronizedMap(
                    new LinkedHashMap<>(MAX_SIZE, 0.75f, true) {
                        @Override
                        protected boolean removeEldestEntry(
                                Map.Entry<String, Object> eldest) {
                            return size() > MAX_SIZE;
                        }
                    }
            );

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) cache.get(key);
    }

    public void evict(String key) {
        cache.remove(key);
    }
}