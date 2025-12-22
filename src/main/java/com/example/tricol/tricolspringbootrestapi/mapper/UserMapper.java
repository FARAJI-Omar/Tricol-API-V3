package com.example.tricol.tricolspringbootrestapi.mapper;

import com.example.tricol.tricolspringbootrestapi.dto.request.RegisterRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.AuthResponse;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "locked", constant = "false")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "userPermissions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserApp toEntity(RegisterRequest request);

    @Mapping(target = "accessToken", ignore = true)
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().getName().name() : null)")
    @Mapping(target = "permissions", expression = "java(calculatePermissions(user))")
    AuthResponse toAuthResponse(UserApp user);

    default java.util.Set<String> calculatePermissions(UserApp user) {
        java.util.Set<String> permissions = new java.util.HashSet<>();

        // Add role permissions
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            user.getRole().getPermissions().forEach(permission ->
                permissions.add(permission.getName().name())
            );
        }

        // Add user-specific permissions
        if (user.getUserPermissions() != null) {
            user.getUserPermissions().stream()
                .filter(up -> up.isActive())
                .forEach(up -> permissions.add(up.getPermission().getName().name()));
        }

        return permissions;
    }
}

