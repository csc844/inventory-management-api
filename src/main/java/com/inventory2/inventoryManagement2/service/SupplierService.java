package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.cache.LruCacheService;
import com.inventory2.inventoryManagement2.dto.SupplierRequestDto;
import com.inventory2.inventoryManagement2.dto.SupplierResponseDto;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.repository.SupplierRepository;
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

    private final SupplierRepository supplierRepository;
    private final StringRedisTemplate redisTemplate;
    private final LruCacheService lruCacheService;
    private final ObjectMapper objectMapper;

    // ---------------- CREATE — writes to Redis cache ----------------

    public SupplierResponseDto createSupplier(SupplierRequestDto dto) {
        log.info("Creating supplier with name: {}", dto.getName());

        Supplier supplier = Supplier.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        Supplier saved = supplierRepository.save(supplier);
        SupplierResponseDto response = mapToDto(saved);

        putToRedis(SUPPLIER_KEY_PREFIX + saved.getId(), response);
        lruCacheService.evict(ALL_SUPPLIERS_KEY);

        log.info("Supplier created with id: {}", saved.getId());
        return response;
    }

    // ---------------- GET ALL — LRU cache ----------------

    public List<SupplierResponseDto> getAllSuppliers() {
        log.info("Fetching all suppliers");

        List<SupplierResponseDto> cached = lruCacheService.get(ALL_SUPPLIERS_KEY);
        if (cached != null) {
            log.debug("LRU cache hit for all suppliers");
            return cached;
        }

        log.debug("LRU cache miss, querying DB");
        List<SupplierResponseDto> result = supplierRepository.findAll()
                .stream().map(this::mapToDto).collect(Collectors.toList());

        lruCacheService.put(ALL_SUPPLIERS_KEY, result);
        return result;
    }

    // ---------------- GET BY ID — Redis cache ----------------

    public SupplierResponseDto getSupplierById(Long id) {
        log.info("Fetching supplier with id: {}", id);
        String key = SUPPLIER_KEY_PREFIX + id;

        SupplierResponseDto cached = getFromRedis(key, SupplierResponseDto.class);
        if (cached != null) {
            log.debug("Redis cache hit for supplier id: {}", id);
            return cached;
        }

        log.debug("Redis cache miss, querying DB for supplier id: {}", id);
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        SupplierResponseDto response = mapToDto(supplier);
        putToRedis(key, response);
        return response;
    }

    // ---------------- UPDATE — writes to Redis cache ----------------

    public SupplierResponseDto updateSupplier(Long id, SupplierRequestDto dto) {
        log.info("Updating supplier with id: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setName(dto.getName());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());

        Supplier updated = supplierRepository.save(supplier);
        SupplierResponseDto response = mapToDto(updated);

        putToRedis(SUPPLIER_KEY_PREFIX + id, response);
        lruCacheService.evict(ALL_SUPPLIERS_KEY);

        log.info("Supplier updated with id: {}", id);
        return response;
    }

    // ---------------- DELETE — evicts from Redis and LRU ----------------

    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with id: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplierRepository.delete(supplier);

        redisTemplate.delete(SUPPLIER_KEY_PREFIX + id);
        lruCacheService.evict(ALL_SUPPLIERS_KEY);

        log.info("Supplier deleted with id: {}", id);
    }

    // ---------------- Redis helpers ----------------

    private void putToRedis(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), Duration.ofMinutes(30));
            log.debug("Saved to Redis: {}", key);
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

    // ---------------- Mapper ----------------

    private SupplierResponseDto mapToDto(Supplier supplier) {
        SupplierResponseDto dto = new SupplierResponseDto();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setTotalProducts(supplier.getProducts() != null ? supplier.getProducts().size() : 0);
        return dto;
    }
}