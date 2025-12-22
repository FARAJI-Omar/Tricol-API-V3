package com.example.tricol.tricolspringbootrestapi.service.impl;

import com.example.tricol.tricolspringbootrestapi.dto.request.LoginRequest;
import com.example.tricol.tricolspringbootrestapi.dto.request.RegisterRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.AuthResponse;
import com.example.tricol.tricolspringbootrestapi.enums.RoleEnum;
import com.example.tricol.tricolspringbootrestapi.exception.DuplicateResourceException;
import com.example.tricol.tricolspringbootrestapi.exception.OperationNotAllowedException;
import com.example.tricol.tricolspringbootrestapi.mapper.UserMapper;
import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.RoleRepository;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import com.example.tricol.tricolspringbootrestapi.security.JwtUtil;
import com.example.tricol.tricolspringbootrestapi.service.AuditService;
import com.example.tricol.tricolspringbootrestapi.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        UserApp user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        if (userRepository.count() == 0) {
            roleRepository.findByName(RoleEnum.ADMIN)
                    .ifPresent(user::setRole);
        }

        user = userRepository.save(user);

        auditService.logAction(user.getUsername(), "REGISTER", user.getId().toString(), "USER");

        return userMapper.toAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            UserApp user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow();

            if (user.getRole() == null) {
                auditService.logAction(request.getUsername(), "LOGIN_FAILURE", "NO_ROLE", "USER");
                throw new OperationNotAllowedException("User does not have an assigned role");
            }

            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            Cookie cookie = new Cookie("refreshToken", refreshToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/api/auth/refresh");
            cookie.setMaxAge((int) jwtService.getRefreshTokenExpirationInSeconds());

            response.addCookie(cookie);

            auditService.logAction(user.getUsername(), "LOGIN", user.getId().toString(), "USER");

            AuthResponse authResponse = userMapper.toAuthResponse(user);
            authResponse.setAccessToken(accessToken);

            return authResponse;

        } catch (AuthenticationException e) {
            auditService.logAction(request.getUsername(), "LOGIN_FAILURE", "AUTH_ERROR", "USER");
            throw e;
        }
    }
}
