package com.example.tricol.tricolspringbootrestapi.security;

import com.example.tricol.tricolspringbootrestapi.model.Permission;
import com.example.tricol.tricolspringbootrestapi.model.RoleApp;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.model.UserPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthorityService {
    public Set<GrantedAuthority> buildAuthorities(UserApp user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (user == null) return authorities;

        RoleApp role = user.getRole();
        Set<UserPermission> userPermissions = user.getUserPermissions();

        if (role != null && role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                boolean revoked = false;
                if (userPermissions != null) {
                    revoked = userPermissions.stream()
                            .filter(Objects::nonNull)
                            .anyMatch(up -> up.getPermission() != null
                                    && Objects.equals(up.getPermission().getId(), permission.getId())
                                    && !up.isActive());
                }
                if (!revoked) {
                    authorities.add(permission);
                }
            }
        }

        if (userPermissions != null) {
            userPermissions.stream()
                    .filter(UserPermission::isActive)
                    .map(UserPermission::getPermission)
                    .filter(Objects::nonNull)
                    .forEach(authorities::add);
        }

        return authorities;
    }
}
