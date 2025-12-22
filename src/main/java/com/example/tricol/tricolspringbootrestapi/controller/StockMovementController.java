package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.response.ErrorResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.StockMovementResponse;
import com.example.tricol.tricolspringbootrestapi.model.StockMovement;
import com.example.tricol.tricolspringbootrestapi.service.StockMovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/stock/movements")
@RequiredArgsConstructor
@Tag(name = "Stock Movement Management", description = "APIs for tracking and searching stock movements including entries, exits, and transfers")
public class StockMovementController {
    
    private final StockMovementService stockMovementService;
    
    @Operation(
            summary = "Search stock movements",
            description = "Search and filter stock movements by date range, product, reference, type, and lot number with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock movements retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "204", description = "No movements found matching the criteria"),
            @ApiResponse(responseCode = "403", description = "Access denied - missing STOCK_READ permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public ResponseEntity<Page<StockMovementResponse>> searchMovements(
            @Parameter(description = "Start date for filtering movements (ISO date format)", example = "2025-01-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @Parameter(description = "End date for filtering movements (ISO date format)", example = "2025-12-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate,
            
            @Parameter(description = "Filter by product ID", example = "1")
            @RequestParam(required = false)
            Long productId,
            
            @Parameter(description = "Filter by movement reference (order number, exit slip number, etc.)", example = "ORD-001")
            @RequestParam(required = false)
            String reference,
            
            @Parameter(description = "Filter by movement type (ENTRY, EXIT, ADJUSTMENT)", example = "ENTRY")
            @RequestParam(required = false)
            StockMovement.Type type,
            
            @Parameter(description = "Filter by lot number", example = "LOT-2025-001")
            @RequestParam(required = false)
            String lotNumber,
            
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0")
            int page,
            
            @Parameter(description = "Page size (number of items per page)", example = "10")
            @RequestParam(defaultValue = "10")
            int size) {
        
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;
        
        Page<StockMovementResponse> movements = stockMovementService.searchMovements(
                startDateTime, endDateTime, productId, reference, type, lotNumber, 
                PageRequest.of(page, size));
        
        if (movements.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(movements);
    }
}
