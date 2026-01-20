package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.request.ProductDTO;
import com.example.tricol.tricolspringbootrestapi.dto.request.UpdateProductRequest;
import com.example.tricol.tricolspringbootrestapi.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PRODUIT_CREATE')")
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductDTO productDTO){
        productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product Created Successfully");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_READ')")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id){
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.status(HttpStatus.OK).body(product);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUIT_READ')")
    public ResponseEntity<List<ProductDTO>> getProducts(){
        List<ProductDTO> products = productService.getProducts();
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_UPDATE')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest updateProductRequest) {
        ProductDTO updatedProduct = productService.updateProduct(id, updateProductRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_DELETE')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.status(HttpStatus.OK).body("deleted successfully");
    }

    @GetMapping("/stock/{id}")
    @PreAuthorize("hasAuthority('PRODUIT_READ')")
    public ResponseEntity<Double> getProductStock(@PathVariable Long id){
        Double stock = productService.getProductStock(id);
        return ResponseEntity.status(HttpStatus.OK).body(stock);
    }

    @GetMapping("/lowstock")
    @PreAuthorize("hasAuthority('PRODUIT_READ')")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(){
        List<ProductDTO> lowStockProducts = productService.getLowStockProducts();
        return ResponseEntity.status(HttpStatus.OK).body(lowStockProducts);
    }
}
