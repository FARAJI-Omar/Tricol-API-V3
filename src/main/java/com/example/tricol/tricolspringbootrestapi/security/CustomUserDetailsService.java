package com.example.tricol.tricolspringbootrestapi.security;

import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import com.example.tricol.tricolspringbootrestapi.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserApp user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<String> permissions = permissionService.calculateFinalPermissions(user);
        
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()))
                .accountExpired(!user.getEnabled())
                .accountLocked(!user.getEnabled())
                .credentialsExpired(false)
                .disabled(!user.getEnabled())
                .build();
    }
}
