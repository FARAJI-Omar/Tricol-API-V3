package com.example.tricol.tricolspringbootrestapi.security;

import com.example.tricol.tricolspringbootrestapi.model.UserApp;
import com.example.tricol.tricolspringbootrestapi.repository.UserRepository;
import com.example.tricol.tricolspringbootrestapi.service.PermissionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        System.out.println("Auth Header: " + authHeader);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No valid Bearer token found");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("Token extracted: " + token.substring(0, 20) + "...");
        
        try {
            boolean isValid = jwtUtil.validateToken(token);
            System.out.println("Token valid: " + isValid);
            
            if (isValid) {
                String username = jwtUtil.extractUsername(token);
                UserApp user = userRepository.findByUsername(username).orElse(null);
                
                if (user != null) {
                    Set<String> permissions = permissionService.calculateFinalPermissions(user);
                    System.out.println("Username: " + username + ", Permissions: " + permissions.size());
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
                    );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("Authentication set successfully");
                }
            }
        } catch (Exception e) {
            System.out.println("JWT validation error: " + e.getMessage());
            e.printStackTrace();
        }
        
        filterChain.doFilter(request, response);
    }
}
