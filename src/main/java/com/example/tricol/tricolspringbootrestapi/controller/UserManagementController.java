package com.example.tricol.tricolspringbootrestapi.controller;

import com.example.tricol.tricolspringbootrestapi.dto.request.UserPermissionRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.UserPermissionResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.UserResponse;
import com.example.tricol.tricolspringbootrestapi.security.CustomUserDetails;
import com.example.tricol.tricolspringbootrestapi.service.UserManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userManagementService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<UserPermissionResponse> assignPermission(@Valid @RequestBody UserPermissionRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserPermissionResponse response = userManagementService.assignPermissionToUser(request, userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> removePermission(@PathVariable Long userId, @PathVariable Long permissionId) {
        userManagementService.removePermissionFromUser(userId, permissionId);
        return ResponseEntity.ok("Permission removed successfully");
    }

    @PatchMapping("/{userId}/permissions/{permissionId}/activate")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> activatePermission(@PathVariable Long userId, @PathVariable Long permissionId) {
        userManagementService.activatePermission(userId, permissionId);
        return ResponseEntity.ok("Permission activated successfully");
    }

    @PatchMapping("/{userId}/permissions/{permissionId}/deactivate")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> deactivatePermission(@PathVariable Long userId, @PathVariable Long permissionId) {
        userManagementService.deactivatePermission(userId, permissionId);
        return ResponseEntity.ok("Permission deactivated successfully");
    }

    @PostMapping("/{userId}/role/{roleId}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ResponseEntity<String> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userManagementService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok("Role assigned successfully");
    }
}

