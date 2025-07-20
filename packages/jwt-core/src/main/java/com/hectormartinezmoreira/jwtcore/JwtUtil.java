package com.hectormartinezmoreira.jwtcore;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.*;

public class JwtUtil {
    private final JwtProperties props;

    public JwtUtil(JwtProperties props) {
        this.props = props;
    }

    private Key getAccessSecretKey() {
        return new SecretKeySpec(props.getAccessSecret().getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }
    private Key getRefreshSecretKey() {
        return new SecretKeySpec(props.getRefreshSecret().getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    // ACCESS TOKEN
    public String generateAccessToken(UUID userId, String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUser", userId.toString());
        claims.put("email", email);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId.toString())
                .setExpiration(new Date(System.currentTimeMillis() + props.getAccessExpiration()))
                .signWith(getAccessSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidAccessToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getAccessSecretKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims extractAllClaimsFromAccessToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getAccessSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID extractUserIdFromAccessToken(String token) {
        Claims claims = extractAllClaimsFromAccessToken(token);
        String userIdStr = claims.get("idUser", String.class);
        if (userIdStr == null) userIdStr = claims.getSubject();
        return UUID.fromString(userIdStr);
    }

    public UserClaimData extractUserClaimDataFromAccessToken(String token) {
        Claims claims = extractAllClaimsFromAccessToken(token);
        String userIdStr = claims.get("idUser", String.class);
        if (userIdStr == null) userIdStr = claims.getSubject();
        // Evitar warning unchecked: cast explícito a List<String>
        Object rolesObj = claims.get("roles");
        List<String> roles;
        if (rolesObj instanceof List<?>) {
            roles = ((List<?>) rolesObj).stream()
                    .map(Object::toString)
                    .toList();
        } else {
            roles = List.of();
        }
        return UserClaimData.builder()
                .idUser(UUID.fromString(userIdStr))
                .email(claims.get("email", String.class))
                .roles(roles)
                .build();
    }

    // REFRESH TOKEN
    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .setExpiration(new Date(System.currentTimeMillis() + props.getRefreshExpiration()))
                .signWith(getRefreshSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidRefreshToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getRefreshSecretKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public UUID extractUserIdFromRefreshToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getRefreshSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return UUID.fromString(claims.getSubject());
    }

    // Helper
    public String extractAccessTokenFromHeader(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
