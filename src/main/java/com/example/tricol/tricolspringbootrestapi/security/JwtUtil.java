package com.example.tricol.tricolspringbootrestapi.security;

import com.example.tricol.tricolspringbootrestapi.enums.JwtTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${keycloak.server-url:}")
    private String keycloakServerUrl;

    @Lazy
    @Autowired(required = false)
    private JwtDecoder keycloakJwtDecoder;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, refreshExpiration);
    }

    public long getRefreshTokenExpirationInSeconds() {
        return refreshExpiration / 1000;
    }

    private String buildToken(UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Keycloak Token Detection and Validation
    public JwtTokenType detectTokenType(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String issuer = claims.getIssuer();
            
            if (issuer != null && (issuer.contains("keycloak") || issuer.contains(keycloakServerUrl))) {
                return JwtTokenType.KEYCLOAK;
            }
            return JwtTokenType.CUSTOM;
        } catch (JwtException e) {
            if (keycloakJwtDecoder != null) {
                try {
                    keycloakJwtDecoder.decode(token);
                    return JwtTokenType.KEYCLOAK;
                } catch (Exception ex) {
                    log.error("Unable to determine token type: {}", ex.getMessage());
                }
            }
            return JwtTokenType.CUSTOM;
        }
    }

    public boolean validateKeycloakToken(String token) {
        if (keycloakJwtDecoder == null) {
            return false;
        }
        try {
            keycloakJwtDecoder.decode(token);
            return true;
        } catch (Exception e) {
            log.error("Keycloak token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
