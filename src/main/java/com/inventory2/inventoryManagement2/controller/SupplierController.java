package com.inventory2.inventoryManagement2.controller;

import com.inventory2.inventoryManagement2.entity.Supplier;
import com.inventory2.inventoryManagement2.service.SupplierService;
import com.inventory2.inventoryManagement2.util.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/suppliers")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSupplier(
            @Valid @RequestBody Supplier supplier) {

        supplierService.createSupplier(supplier);

        return ResponseUtil.success("Supplier created successfully");
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
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @Valid @RequestBody Supplier supplier) {

        supplierService.updateSupplier(id, supplier);

        return ResponseUtil.success("Supplier updated successfully");
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id) {

        supplierService.deleteSupplier(id);

        return ResponseUtil.success("Supplier deleted successfully");
    }
}






