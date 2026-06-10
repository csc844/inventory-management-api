package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.dto.SupplierRequestDto;
import com.inventory2.inventoryManagement2.dto.SupplierResponseDto;
import com.inventory2.inventoryManagement2.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupplierController {

    private final SupplierService supplierService;

    // CREATE
    @PostMapping
    public SupplierResponseDto createSupplier(@RequestBody SupplierRequestDto dto) {
        return supplierService.createSupplier(dto);
    }

    // GET ALL
    @GetMapping
    public List<SupplierResponseDto> getAll() {
        return supplierService.getAllSuppliers();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public SupplierResponseDto getById(@Valid @PathVariable Long id) {
        return supplierService.getSupplierById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public SupplierResponseDto update(@Valid @PathVariable Long id,
                                      @Valid @RequestBody SupplierRequestDto dto) {
        return supplierService.updateSupplier(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@Valid @PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "Supplier deleted successfully";
    }
}






