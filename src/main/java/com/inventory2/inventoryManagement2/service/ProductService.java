package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.dto.ProductRequestDto;
import com.inventory2.inventoryManagement2.dto.ProductResponseDto;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import com.inventory2.inventoryManagement2.repository.ProductRepository;
import com.inventory2.inventoryManagement2.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class  ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductResponseDto createProduct(ProductRequestDto dto) {
        log.info("Creating product with name: {} for supplierId: {}", dto.getName(), dto.getSupplierId());

        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> {
                    log.warn("Supplier not found with id: {}", dto.getSupplierId());
                    return new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId());
                });

        Product product = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .supplier(supplier)
                .status(null)
                .createdAt(LocalDateTime.now())
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created successfully with id: {}", saved.getId());

        ProductResponseDto response = new ProductResponseDto();
        response.setId(saved.getId());
        response.setName(saved.getName());
        response.setPrice(saved.getPrice());
        response.setSupplierName(supplier.getName());
        response.setStatus(String.valueOf(saved.getStatus()));
        response.setCreatedAt(saved.getCreatedAt());

        return response;
    }
}