package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.dto.request.UserPermissionRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.UserPermissionResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.UserResponse;
import com.example.tricol.tricolspringbootrestapi.dto.response.RoleInfo;
import com.example.tricol.tricolspringbootrestapi.dto.response.PermissionInfo;
import com.example.tricol.tricolspringbootrestapi.model.Permission;
import com.example.tricol.tricolspringbootrestapi.model.RoleApp;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.model.UserPermission;
import com.example.tricol.tricolspringbootrestapi.exception.DuplicateResourceException;
import com.example.tricol.tricolspringbootrestapi.exception.OperationNotAllowedException;
import com.example.tricol.tricolspringbootrestapi.exception.ResourceNotFoundException;
import com.example.tricol.tricolspringbootrestapi.mapper.UserPermissionMapper;
import com.example.tricol.tricolspringbootrestapi.repository.PermissionRepository;
import com.example.tricol.tricolspringbootrestapi.repository.RoleRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserPermissionRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import com.example.tricol.tricolspringbootrestapi.security.CustomUserDetails;
import com.example.tricol.tricolspringbootrestapi.service.AuditService;
import com.example.tricol.tricolspringbootrestapi.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserPermissionMapper userPermissionMapper;
    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public UserPermissionResponse assignPermissionToUser(UserPermissionRequest request, Long adminId) {
        UserApp user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        if (userPermissionRepository.findByUserIdAndPermissionId(request.getUserId(), request.getPermissionId()).isPresent()) {
            throw new DuplicateResourceException("Permission already assigned to user");
        }

        UserPermission userPermission = UserPermission.builder()
                .user(user)
                .permission(permission)
                .active(true)
                .grantedBy(adminId)
                .build();

        userPermission = userPermissionRepository.save(userPermission);

        auditService.logPermissionChange(user.getId(), user.getUsername(),
                permission.getName().name(), true, adminId);

        return userPermissionMapper.toResponse(userPermission);
    }

    @Override
    @Transactional
    public void removePermissionFromUser(Long userId, Long permissionId) {
        UserPermission userPermission = userPermissionRepository.findByUserIdAndPermissionId(userId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("User permission not found"));

        Long adminId = getCurrentUserId();
        UserApp user = userPermission.getUser();
        Permission permission = userPermission.getPermission();

        userPermissionRepository.delete(userPermission);

        auditService.logPermissionChange(user.getId(), user.getUsername(),
                permission.getName().name(), false, adminId);
    }

    @Override
    @Transactional
    public void activatePermission(Long userId, Long permissionId) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        UserPermission userPermission = userPermissionRepository.findByUserIdAndPermissionId(userId, permissionId)
                .orElseGet(() -> UserPermission.builder()
                        .user(user)
                        .permission(permission)
                        .build());

        if (userPermission.getId() != null && userPermission.isActive()) {
            throw new OperationNotAllowedException("Permission is already active");
        }

        userPermission.setActive(true);
        userPermission.setRevokedAt(null);
        userPermissionRepository.save(userPermission);

        Long adminId = getCurrentUserId();
        auditService.logPermissionChange(user.getId(), user.getUsername(),
                permission.getName().name(), true, adminId);
    }

    @Override
    @Transactional
    public void deactivatePermission(Long userId, Long permissionId) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found"));

        UserPermission userPermission = userPermissionRepository.findByUserIdAndPermissionId(userId, permissionId)
                .orElseGet(() -> UserPermission.builder()
                        .user(user)
                        .permission(permission)
                        .build());

        if (userPermission.getId() != null && !userPermission.isActive()) {
            throw new OperationNotAllowedException("Permission is already deactivated");
        }

        userPermission.setActive(false);
        userPermission.setRevokedAt(LocalDateTime.now());
        userPermissionRepository.save(userPermission);

        Long adminId = getCurrentUserId();
        auditService.logPermissionChange(user.getId(), user.getUsername(),
                permission.getName().name(), false, adminId);
    }

    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != null) {
            throw new DuplicateResourceException("User already has a role");
        }

        RoleApp role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        user.setRole(role);
        userRepository.save(user);

        // Automatically grant all role permissions to the user
        Long adminId = getCurrentUserId();
        for (Permission permission : role.getPermissions()) {
            // Check if user already has this permission
            if (userPermissionRepository.findByUserIdAndPermissionId(userId, permission.getId()).isEmpty()) {
                UserPermission userPermission = UserPermission.builder()
                        .user(user)
                        .permission(permission)
                        .active(true)
                        .grantedBy(adminId)
                        .build();
                userPermissionRepository.save(userPermission);

                auditService.logPermissionChange(user.getId(), user.getUsername(),
                        permission.getName().name(), true, adminId);
            }
        }

        auditService.logAction(user.getUsername(), "ROLE_ASSIGNED", userId.toString(), "USER");
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .role(user.getRole() != null ? RoleInfo.builder()
                                .id(user.getRole().getId())
                                .name(user.getRole().getName().name())
                                .build() : null)
                        .permissions(user.getUserPermissions().stream()
                                .map(up -> PermissionInfo.builder()
                                        .id(up.getPermission().getId())
                                        .name(up.getPermission().getName().name())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleInfo> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> RoleInfo.builder()
                        .id(role.getId())
                        .name(role.getName().name())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionInfo> getRolePermissions(Long roleId) {
        RoleApp role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        return role.getPermissions().stream()
                .map(permission -> PermissionInfo.builder()
                        .id(permission.getId())
                        .name(permission.getName().name())
                        .active(true)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionInfo> getUserPermissions(Long userId) {
        UserApp user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getUserPermissions().stream()
                .map(up -> PermissionInfo.builder()
                        .id(up.getPermission().getId())
                        .name(up.getPermission().getName().name())
                        .active(up.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getId();
        }
        return null;
    }
}
