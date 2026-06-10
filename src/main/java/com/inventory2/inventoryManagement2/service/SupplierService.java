package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.dto.SupplierRequestDto;
import com.inventory2.inventoryManagement2.dto.SupplierResponseDto;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupplierService {

    private final SupplierRepository supplierRepository;

    // ---------------- CREATE SUPPLIER ----------------

    public SupplierResponseDto createSupplier(SupplierRequestDto dto) {
        log.info("Creating supplier with name: {}", dto.getName());

        Supplier supplier = Supplier.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        Supplier saved = supplierRepository.save(supplier);
        log.info("Supplier created successfully with id: {}", saved.getId());

        return mapToDto(saved);
    }

    // ---------------- GET ALL ----------------

    public List<SupplierResponseDto> getAllSuppliers() {
        log.info("Fetching all suppliers");

        List<SupplierResponseDto> suppliers = supplierRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        log.debug("Total suppliers fetched: {}", suppliers.size());
        return suppliers;
    }

    // ---------------- GET BY ID ----------------

    public SupplierResponseDto getSupplierById(Long id) {
        log.info("Fetching supplier with id: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Supplier not found with id: {}", id);
                    return new ResourceNotFoundException("Supplier not found with id: " + id);
                });

        return mapToDto(supplier);
    }

    // ---------------- UPDATE ----------------

    public SupplierResponseDto updateSupplier(Long id, SupplierRequestDto dto) {
        log.info("Updating supplier with id: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Supplier not found with id: {}", id);
                    return new ResourceNotFoundException("Supplier not found with id: " + id);
                });

        supplier.setName(dto.getName());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());

        Supplier updated = supplierRepository.save(supplier);
        log.info("Supplier updated successfully with id: {}", updated.getId());

        return mapToDto(updated);
    }

    // ---------------- DELETE ----------------

    public void deleteSupplier(Long id) {
        log.info("Deleting supplier with id: {}", id);

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Supplier not found with id: {}", id);
                    return new ResourceNotFoundException("Supplier not found with id: " + id);
                });

        supplierRepository.delete(supplier);
        log.info("Supplier deleted successfully with id: {}", id);
    }

    // ---------------- MAPPER ----------------
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