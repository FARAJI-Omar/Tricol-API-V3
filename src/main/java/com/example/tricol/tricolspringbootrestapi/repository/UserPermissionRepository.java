package com.example.tricol.tricolspringbootrestapi.repository;

import com.example.tricol.tricolspringbootrestapi.model.Permission;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.model.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUser(UserApp user);
    Optional<UserPermission> findByUserAndPermission(UserApp user, Permission permission);
    Optional<UserPermission> findByUserIdAndPermissionId(Long userId, Long permissionId);
}
