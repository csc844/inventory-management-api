package com.inventory2.inventoryManagement2.cache;

import com.inventory2.inventoryManagement2.dto.SupplierResponseDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LruCacheService {

    private static final int MAX_SIZE = 100;

    // access-order=true makes this an LRU cache: least recently accessed entry is evicted first
    private final Map<String, List<SupplierResponseDto>> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(MAX_SIZE, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<SupplierResponseDto>> eldest) {
                    return size() > MAX_SIZE;
                }
            }
    );

    public void put(String key, List<SupplierResponseDto> value) {
        cache.put(key, value);
    }

    public List<SupplierResponseDto> get(String key) {
        return cache.get(key);
    }

    public void evict(String key) {
        cache.remove(key);
    }
}