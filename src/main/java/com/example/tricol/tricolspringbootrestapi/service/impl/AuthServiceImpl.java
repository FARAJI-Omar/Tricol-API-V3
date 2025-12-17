package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.dto.request.LoginRequest;
import com.example.tricol.tricolspringbootrestapi.dto.request.RegisterRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.AuthResponse;
import com.example.tricol.tricolspringbootrestapi.exception.DuplicateResourceException;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import com.example.tricol.tricolspringbootrestapi.security.JwtUtil;
import com.example.tricol.tricolspringbootrestapi.service.AuditService;
import com.example.tricol.tricolspringbootrestapi.service.AuthService;
import com.example.tricol.tricolspringbootrestapi.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PermissionService permissionService;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    @Override
    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        UserApp user = new UserApp();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setRole(null);

        userRepository.save(user);

        auditService.logAction(user.getUsername(), "REGISTER", user.getId().toString(), "UserApp");

        return "User registered successfully";
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserApp user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> permissions = permissionService.calculateFinalPermissions(user);
        String role = user.getRole() != null ? user.getRole().getName().name() : null;
        String token = jwtUtil.generateToken(user.getUsername(), role, permissions);

        auditService.logAction(user.getUsername(), "LOGIN", user.getId().toString(), "UserApp");

        return new AuthResponse(token, user.getUsername(), role, permissions);
    }
}
