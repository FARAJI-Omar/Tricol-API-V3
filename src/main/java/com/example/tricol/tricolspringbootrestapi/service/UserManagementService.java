package com.example.tricol.tricolspringbootrestapi.service;

import com.example.tricol.tricolspringbootrestapi.dto.request.UserPermissionRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.UserPermissionResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.UserResponse;

import java.util.List;

public interface UserManagementService {
    UserPermissionResponse assignPermissionToUser(UserPermissionRequest request, Long adminId);
    void removePermissionFromUser(Long userId, Long permissionId);
    void activatePermission(Long userId, Long permissionId);
    void deactivatePermission(Long userId, Long permissionId);
    void assignRoleToUser(Long userId, Long roleId);
    List<UserResponse> getAllUsers();
}