package com.example.tricol.tricolspringbootrestapi.service;

import com.example.tricol.tricolspringbootrestapi.dto.request.LoginRequest;
import com.example.tricol.tricolspringbootrestapi.dto.request.RegisterRequest;
import com.example.tricol.tricolspringbootrestapi.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, HttpServletResponse response);
}
