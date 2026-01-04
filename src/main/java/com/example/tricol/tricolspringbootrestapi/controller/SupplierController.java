package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.request.SupplierDTO;
import com.example.tricol.tricolspringbootrestapi.model.Supplier;
import com.example.tricol.tricolspringbootrestapi.service.SupplierServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    @Autowired
    private SupplierServiceInterface supplierService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('FOURNISSEUR_CREATE')")
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
        Supplier supplier = supplierService.createSupplier(supplierDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplier);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FOURNISSEUR_READ')")
    public ResponseEntity<List<SupplierDTO>> getAllSuppliers() {
        List<SupplierDTO> suppliers = supplierService.getSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FOURNISSEUR_READ')")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        SupplierDTO supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('FOURNISSEUR_UPDATE')")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDTO supplierDTO) {
        SupplierDTO updatedSupplier = supplierService.updateSupplier(id, supplierDTO);
        return ResponseEntity.ok(updatedSupplier);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('FOURNISSEUR_DELETE')")
    public ResponseEntity<String> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok("deleted successfully");
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('FOURNISSEUR_READ')")
    public ResponseEntity<List<SupplierDTO>> searchSuppliers(@RequestParam("q") String query) {
        List<SupplierDTO> suppliers = supplierService.searchSuppliers(query);
        return ResponseEntity.ok(suppliers);
    }
}