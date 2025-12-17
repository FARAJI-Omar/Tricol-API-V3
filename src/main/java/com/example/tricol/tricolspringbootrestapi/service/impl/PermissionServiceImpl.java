package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.model.Permission;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.model.UserPermission;
import com.example.tricol.tricolspringbootrestapi.service.PermissionService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Override
    public Set<String> calculateFinalPermissions(UserApp user) {
        Set<String> finalPermissions = new HashSet<>();
        
        // If user has no role, return empty set
        if (user.getRole() == null) {
            return finalPermissions;
        }
        
        // Start with role permissions
        finalPermissions = user.getRole().getPermissions().stream()
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        
        // Apply user-specific overrides
        for (UserPermission userPermission : user.getUserPermissions()) {
            String permissionCode = userPermission.getPermission().getCode();
            
            if (userPermission.getEnabled()) {
                // Add permission
                finalPermissions.add(permissionCode);
            } else {
                // Remove permission
                finalPermissions.remove(permissionCode);
            }
        }
        
        return finalPermissions;
    }
}
