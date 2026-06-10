package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.dto.SupplierRequestDto;
import com.inventory2.inventoryManagement2.dto.SupplierResponseDto;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.repository.SupplierRepository;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupplierService {

    private final SupplierRepository supplierRepository;

    // ---------------- CREATE SUPPLIER ----------------

    public SupplierResponseDto createSupplier(SupplierRequestDto dto) {

        Supplier supplier = Supplier.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        Supplier saved = supplierRepository.save(supplier);

        return mapToDto(saved);
    }

    // ---------------- GET ALL ----------------

    public List<SupplierResponseDto> getAllSuppliers() {

        return supplierRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ---------------- GET BY ID ----------------

    public SupplierResponseDto getSupplierById(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        return mapToDto(supplier);
    }

    // ---------------- UPDATE ----------------

    public SupplierResponseDto updateSupplier(Long id, SupplierRequestDto dto) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplier.setName(dto.getName());
        supplier.setEmail(dto.getEmail());
        supplier.setPhone(dto.getPhone());

        Supplier updated = supplierRepository.save(supplier);

        return mapToDto(updated);
    }

    // ---------------- DELETE ----------------

    public void deleteSupplier(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        supplierRepository.delete(supplier);
    }

    // ---------------- MAPPER ----------------
    private SupplierResponseDto mapToDto(Supplier supplier) {

        SupplierResponseDto dto = new SupplierResponseDto();

        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());

        // If products list exists
        dto.setTotalProducts(
                supplier.getProducts() != null ? supplier.getProducts().size() : 0
        );

        return dto;
    }
}