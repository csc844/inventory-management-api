package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<String> createSupplier(
            @Valid @RequestBody Supplier supplier) {

        supplierService.createSupplier(supplier);

        return ResponseEntity.ok("Supplier created successfully");
    }

    @GetMapping
    public List<Supplier> getAll() {
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable Long id) {
        return supplierService.getSupplierById(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @PathVariable Long id,
            @Valid @RequestBody Supplier supplier) {

        supplierService.updateSupplier(id, supplier);

        return ResponseEntity.ok("Supplier updated successfully");
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@Valid @PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return "Supplier deleted successfully";
    }
}






