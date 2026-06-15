package com.inventory2.inventoryManagement2.cache;

import com.inventory2.inventoryManagement2.util.Constants;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LruCacheService {

    private final Map<String, Object> cache =
            Collections.synchronizedMap(
                    new LinkedHashMap<>(
                            Constants.LRU_CACHE_MAX_SIZE,
                            Constants.LRU_CACHE_LOAD_FACTOR,
                            true) {

                        @Override
                        protected boolean removeEldestEntry(
                                Map.Entry<String, Object> eldest) {

                            return size() > Constants.LRU_CACHE_MAX_SIZE;
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