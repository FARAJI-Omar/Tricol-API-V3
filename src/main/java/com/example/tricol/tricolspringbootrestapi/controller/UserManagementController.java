package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.request.UserPermissionRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.ErrorResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.UserPermissionResponse;
import com.example.tricol.tricolspringbootrestapi.security.CustomUserDetails;
import com.example.tricol.tricolspringbootrestapi.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing user permissions and roles (Admin only)")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(
            summary = "Assign permission to user",
            description = "Grants a specific permission directly to a user (separate from role permissions)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission assigned successfully",
                    content = @Content(schema = @Schema(implementation = UserPermissionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or permission already assigned",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing USER_MANAGE permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or permission not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<UserPermissionResponse> assignPermission(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User permission assignment request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserPermissionRequest.class))
            )
            @Valid @RequestBody UserPermissionRequest request,
            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserPermissionResponse response = userManagementService.assignPermissionToUser(request, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Remove permission from user",
            description = "Removes a directly assigned permission from a user (does not affect role permissions)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission removed successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - missing USER_MANAGE permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User permission not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{userId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> removePermission(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "ID of the permission to remove", required = true, example = "5")
            @PathVariable Long permissionId) {
        userManagementService.removePermissionFromUser(userId, permissionId);
        return ResponseEntity.ok("Permission removed successfully");
    }

    @Operation(
            summary = "Activate user permission",
            description = "Activates a previously deactivated user permission or creates a new active permission"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission activated successfully"),
            @ApiResponse(responseCode = "400", description = "Permission is already active",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing USER_MANAGE permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or permission not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{userId}/permissions/{permissionId}/activate")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> activatePermission(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "ID of the permission to activate", required = true, example = "5")
            @PathVariable Long permissionId) {
        userManagementService.activatePermission(userId, permissionId);
        return ResponseEntity.ok("Permission activated successfully");
    }

    @Operation(
            summary = "Deactivate user permission",
            description = "Deactivates a user permission without removing it (can be reactivated later)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permission deactivated successfully"),
            @ApiResponse(responseCode = "400", description = "Permission is already deactivated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing USER_MANAGE permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or permission not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{userId}/permissions/{permissionId}/deactivate")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> deactivatePermission(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "ID of the permission to deactivate", required = true, example = "5")
            @PathVariable Long permissionId) {
        userManagementService.deactivatePermission(userId, permissionId);
        return ResponseEntity.ok("Permission deactivated successfully");
    }

    @Operation(
            summary = "Assign role to user",
            description = "Assigns a role to a user (user can only have one role at a time)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role assigned successfully"),
            @ApiResponse(responseCode = "400", description = "User already has a role",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - missing USER_MANAGE permission",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{userId}/role/{roleId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> assignRole(
            @Parameter(description = "ID of the user", required = true, example = "1")
            @PathVariable Long userId,
            @Parameter(description = "ID of the role to assign", required = true, example = "2")
            @PathVariable Long roleId) {
        userManagementService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok("Role assigned successfully");
    }
}

