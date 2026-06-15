package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.cache.LruCacheService;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.repository.GenericRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SupplierService {

    private static final String SUPPLIER_KEY_PREFIX = "supplier:";
    private static final String ALL_SUPPLIERS_KEY = "all_suppliers";

    private final GenericRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final LruCacheService lruCacheService;
    private final ObjectMapper objectMapper;

    // ---------------- CREATE — writes to Redis cache ----------------

    public Supplier createSupplier(Supplier supplier) {

        log.info("Creating supplier with name: {}", supplier.getName());

        Supplier saved = repository.save(supplier);

        putToRedis(SUPPLIER_KEY_PREFIX + saved.getId(), saved);
        lruCacheService.evict(ALL_SUPPLIERS_KEY);

        log.info("Supplier created with id: {}", saved.getId());

        return saved;
    }
    // ---------------- GET ALL — LRU cache ----------------

    public List<Supplier> getAllSuppliers() {

        List<Supplier> cached = lruCacheService.get(ALL_SUPPLIERS_KEY);

        if (cached != null) {
            log.info("LRU cache hit for all suppliers");
            return cached;
        }

        log.info("LRU cache miss, querying DB");

        List<Supplier> result = repository.findAll(Supplier.class);

        lruCacheService.put(ALL_SUPPLIERS_KEY, result);

        return result;
    }
    // ---------------- GET BY ID — Redis cache ----------------

    public Supplier getSupplierById(Long id) {

        log.info("Fetching supplier with id: {}", id);

        String key = SUPPLIER_KEY_PREFIX + id;

        Supplier cached = getFromRedis(key, Supplier.class);

        if (cached != null) {
            log.info("Redis cache hit for supplier id: {}", id);
            return cached;
        }

        Supplier supplier = repository
                .findById(Supplier.class, id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found with id: " + id));

        putToRedis(key, supplier);

        return supplier;
    }

    // ---------------- UPDATE — writes to Redis cache ----------------

    public Supplier updateSupplier(Long id, Supplier request) {

        Supplier supplier = repository
                .findById(Supplier.class, id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found with id: " + id));

        supplier.setName(request.getName());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());

        Supplier updated = repository.save(supplier);

        putToRedis(SUPPLIER_KEY_PREFIX + id, updated);
        lruCacheService.evict(ALL_SUPPLIERS_KEY);

        return updated;
    }

    // ---------------- DELETE — evicts from Redis and LRU ----------------

    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with id: {}", id);

        Supplier supplier = repository
                .findById(Supplier.class, id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Supplier not found with id: " + id));

        repository.delete(supplier);

        redisTemplate.delete(SUPPLIER_KEY_PREFIX + id);
        lruCacheService.evict(ALL_SUPPLIERS_KEY);

        log.info("Supplier deleted with id: {}", id);
    }

    // ---------------- Redis helpers ----------------

    private void putToRedis(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), Duration.ofMinutes(30));
            log.info("Saved to Redis: {}", key);
        } catch (Exception e) {
            log.warn("Redis write failed for key {}: {}", key, e.getMessage());
        }
    }

    private <T> T getFromRedis(String key, Class<T> type) {
        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) return objectMapper.readValue(json, type);
        } catch (Exception e) {
            log.warn("Redis read failed for key {}: {}", key, e.getMessage());
        }
        return null;
    }
}