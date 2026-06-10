package com.inventory2.inventoryManagement2.service;

import com.inventory2.inventoryManagement2.dto.ProductRequestDto;
import com.inventory2.inventoryManagement2.dto.ProductResponseDto;
import com.inventory2.inventoryManagement2.entity.Product;
import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.repository.ProductRepository;
import com.inventory2.inventoryManagement2.repository.SupplierRepository;
import com.inventory2.inventoryManagement2.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class  ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    public ProductResponseDto createProduct(ProductRequestDto dto) {

        // 1. Find supplier
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + dto.getSupplierId()));



        // 2. Create Product entity
        Product product = Product.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .supplier(supplier)
                .status(null)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. Save product
        Product saved = productRepository.save(product);

        // 4. Convert to response DTO
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