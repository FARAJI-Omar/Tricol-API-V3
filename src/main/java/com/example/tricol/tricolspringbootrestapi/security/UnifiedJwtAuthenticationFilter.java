package com.example.tricol.tricolspringbootrestapi.security;

import com.example.tricol.tricolspringbootrestapi.enums.JwtTokenType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnifiedJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtDecoder keycloakJwtDecoder;
    private final CustomUserDetailsServices userDetailsService;
    private final KeycloakJwtConverter keycloakJwtConverter;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtTokenType tokenType = jwtUtil.detectTokenType(jwt);

            if (tokenType == JwtTokenType.CUSTOM) {
                authenticateWithCustomToken(jwt, request);
            } else if (tokenType == JwtTokenType.KEYCLOAK) {
                authenticateWithKeycloakToken(jwt, request);
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateWithCustomToken(String jwt, HttpServletRequest request) {
        try {
            final String username = jwtUtil.extractUsername(jwt);

            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Custom JWT authentication successful for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("Custom JWT authentication failed: {}", e.getMessage());
        }
    }

    private void authenticateWithKeycloakToken(String jwt, HttpServletRequest request) {
        try {
            Jwt decodedJwt = keycloakJwtDecoder.decode(jwt);
            UsernamePasswordAuthenticationToken authToken = keycloakJwtConverter.convert(decodedJwt);

            if (authToken != null) {
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("Keycloak JWT authentication successful");
            }
        } catch (Exception e) {
            log.error("Keycloak JWT authentication failed: {}", e.getMessage());
        }
    }
}
