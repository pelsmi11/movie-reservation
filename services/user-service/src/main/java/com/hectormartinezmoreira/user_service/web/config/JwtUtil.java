package com.hectormartinezmoreira.user_service.web.config;

import com.hectormartinezmoreira.user_service.domain.dto.security.UserClaimData;
import com.hectormartinezmoreira.user_service.persistence.model.UserEntity;
import com.hectormartinezmoreira.user_service.persistence.model.Role;
import com.hectormartinezmoreira.user_service.persistence.model.UserRole;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtil {
    // Ideally, load secret and expirations from application.properties or environment
    @Value("${movie-service.secret.access}")
    private String accessJwtSecret;
    @Value("${movie-service.secret.refresh}")
    private String refreshJwtSecret;
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    // === ACCESS TOKEN ===
    public String generateAccessToken(UserEntity user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUser", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("roles", user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName()).toList());
        //claims.put("roles", user.getRoles().stream().map(Role::getName).toList());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, accessJwtSecret)
                .compact();
    }

    // === REFRESH TOKEN ===
    public String generateRefreshToken(UserEntity user) {
        // Opcional: puedes agregar solo el id de usuario o nada extra, pero por compatibilidad puedes dejar el subject
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, refreshJwtSecret)
                .compact();
    }

    // === EXTRACT & VALIDATION ===
    // Para el access token
    public boolean isValidAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(accessJwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims extractAllClaimsFromAccessToken(String token) {
        return Jwts.parser()
                .setSigningKey(accessJwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID extractUserIdFromAccessToken(String token) {
        Claims claims = extractAllClaimsFromAccessToken(token);
        String userIdStr = claims.get("idUser", String.class);
        // fallback si no existe el claim (ejemplo si alguien usa un token viejo)
        if (userIdStr == null) userIdStr = claims.getSubject();
        return UUID.fromString(userIdStr);
    }

    public UserClaimData extractUserClaimDataFromAccessToken(String token) {
        Claims claims = extractAllClaimsFromAccessToken(token);
        String userIdStr = claims.get("idUser", String.class);
        // fallback si no existe el claim (ejemplo si alguien usa un token viejo)
        if (userIdStr == null) userIdStr = claims.getSubject();
        return UserClaimData.builder()
                .idUser(UUID.fromString(userIdStr))
                .email(claims.get("email", String.class))
                .roles(claims.get("roles", List.class))
                .build();
    }

    // Para el refresh token
    public boolean isValidRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(refreshJwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Claims extractAllClaimsFromRefreshToken(String token) {
        return Jwts.parser()
                .setSigningKey(refreshJwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID extractUserIdFromRefreshToken(String token) {
        Claims claims = extractAllClaimsFromRefreshToken(token);
        String userIdStr = claims.getSubject();
        return UUID.fromString(userIdStr);
    }

    // Helpers
    public Date getAccessTokenExpiration() {
        return new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION);
    }

    public Date getRefreshTokenExpiration() {
        return new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION);
    }

    public String extractAccessTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7); // Remove "Bearer " prefix
    }
}
