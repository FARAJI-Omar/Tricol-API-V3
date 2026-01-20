package com.example.tricol.tricolspringbootrestapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private Long id;

    private String reference;

    private String name;

    private String description;

    @Min(value = 0, message = "Unit price must be greater than or equal to 0")
    private Double unitPrice;

    private String category;

    private String measureUnit;

    @Min(value = 0, message = "Re-order point must be greater than or equal to 0")
    private Double reorderPoint;

    @Min(value = 0, message = "Stock cannot be below 0")
    private Double currentStock;
}
