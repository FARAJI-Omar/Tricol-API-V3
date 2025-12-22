package com.example.tricol.tricolspringbootrestapi.mapper;

import com.example.tricol.tricolspringbootrestapi.dto.response.UserPermissionResponse;
import com.example.tricol.tricolspringbootrestapi.model.UserPermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserPermissionMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "permissionId", source = "permission.id")
    @Mapping(target = "permissionName", expression = "java(userPermission.getPermission().getName().name())")
    UserPermissionResponse toResponse(UserPermission userPermission);
}

