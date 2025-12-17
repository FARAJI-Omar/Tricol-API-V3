package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.request.CreateOrderRequest;
import com.example.tricol.tricolspringbootrestapi.dto.request.UpdateOrderStatus;
import com.example.tricol.tricolspringbootrestapi.dto.response.ErrorResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.OrderResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.ReceiveOrderResponse;
import com.example.tricol.tricolspringbootrestapi.service.OrderService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Order Management", description = "APIs for managing purchase orders and order processing")
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "Create a new order",
            description = "Creates a new purchase order with supplier and product details"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Supplier or Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<OrderResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order creation request with supplier and items",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateOrderRequest.class))
            )
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse createdOrder = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @Operation(
            summary = "Get all orders",
            description = "Retrieves a list of all orders in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders(){
        List<OrderResponse> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(
            summary = "Get order by ID",
            description = "Retrieves a specific order by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true, example = "1")
            @PathVariable Long id){
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @Operation(
            summary = "Receive an order",
            description = "Marks an order as received and updates stock inventory accordingly"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order received successfully",
                    content = @Content(schema = @Schema(implementation = ReceiveOrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Order already received",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/receive")
    public ResponseEntity<ReceiveOrderResponse> receiveOrder(
            @Parameter(description = "ID of the order to receive", required = true, example = "1")
            @PathVariable Long id){
        ReceiveOrderResponse response = orderService.receiveOrder(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update order status",
            description = "Updates the status of an existing order"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order updated successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @Parameter(description = "ID of the order to update", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated order status",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateOrderStatus.class))
            )
            @Valid @RequestBody UpdateOrderStatus orderDto) {
        OrderResponse updatedOrder = orderService.updateOrder(id, orderDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedOrder);
    }
}
