package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.request.CreateExitSlipRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.ErrorResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.ExitSlipResponse;
import com.example.tricol.tricolspringbootrestapi.enums.ExitSlipStatus;
import com.example.tricol.tricolspringbootrestapi.service.ExitSlipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exit-slips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Exit Slip Management", description = "APIs for managing material exit slips for workshops")
public class ExitSlipController {
    
    private final ExitSlipService exitSlipService;
    
    @Operation(
            summary = "Create a new exit slip",
            description = "Creates a new exit slip for withdrawing materials from stock for a workshop"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Exit slip created successfully",
                    content = @Content(schema = @Schema(implementation = ExitSlipResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing BON_SORTIE_CREATE permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('BON_SORTIE_CREATE')")
    public ResponseEntity<ExitSlipResponse> createExitSlip(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Exit slip creation request with workshop and items",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateExitSlipRequest.class))
            )
            @Valid @RequestBody CreateExitSlipRequest request) {
        ExitSlipResponse response = exitSlipService.createExitSlip(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(
            summary = "Validate an exit slip",
            description = "Validates an exit slip and updates stock accordingly"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exit slip validated successfully",
                    content = @Content(schema = @Schema(implementation = ExitSlipResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition or insufficient stock",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing BON_SORTIE_VALIDATE permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exit slip not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAuthority('BON_SORTIE_VALIDATE')")
    public ResponseEntity<ExitSlipResponse> validateExitSlip(
            @Parameter(description = "ID of the exit slip to validate", required = true, example = "1")
            @PathVariable Long id) {
        ExitSlipResponse response = exitSlipService.validateExitSlip(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Cancel an exit slip",
            description = "Cancels an exit slip and restores stock if already validated"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exit slip cancelled successfully",
                    content = @Content(schema = @Schema(implementation = ExitSlipResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status transition",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing BON_SORTIE_CANCEL permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exit slip not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('BON_SORTIE_CANCEL')")
    public ResponseEntity<ExitSlipResponse> cancelExitSlip(
            @Parameter(description = "ID of the exit slip to cancel", required = true, example = "1")
            @PathVariable Long id) {
        ExitSlipResponse response = exitSlipService.cancelExitSlip(id);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
            summary = "Get exit slip by ID",
            description = "Retrieves detailed information about a specific exit slip"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exit slip retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ExitSlipResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing BON_SORTIE_READ permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Exit slip not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BON_SORTIE_READ')")
    public ResponseEntity<ExitSlipResponse> getExitSlip(
            @Parameter(description = "ID of the exit slip to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        ExitSlipResponse response = exitSlipService.getExitSlip(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all exit slips",
            description = "Retrieves all exit slips with optional filtering by status or workshop"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Exit slips retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ExitSlipResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing BON_SORTIE_READ permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('BON_SORTIE_READ')")
    public ResponseEntity<List<ExitSlipResponse>> getAllExitSlips(
            @Parameter(description = "Filter by status (PENDING, VALIDATED, CANCELLED)", example = "PENDING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by workshop name", example = "Workshop A")
            @RequestParam(required = false) String workshop) {
        
        if (status != null) {
            ExitSlipStatus exitSlipStatus = ExitSlipStatus.valueOf(status.toUpperCase());
            List<ExitSlipResponse> responses = exitSlipService.getExitSlipsByStatus(exitSlipStatus);
            return ResponseEntity.ok(responses);
        }
        
        if (workshop != null) {
            List<ExitSlipResponse> responses = exitSlipService.getExitSlipsByWorkshop(workshop);
            return ResponseEntity.ok(responses);
        }
        
        List<ExitSlipResponse> responses = exitSlipService.getAllExitSlips();
        return ResponseEntity.ok(responses);
    }
}
