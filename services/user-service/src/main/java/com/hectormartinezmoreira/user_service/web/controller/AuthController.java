package com.hectormartinezmoreira.user_service.web.controller;

import com.hectormartinezmoreira.user_service.domain.dto.request.LoginRequest;
import com.hectormartinezmoreira.user_service.domain.dto.request.RefreshTokenRequest;
import com.hectormartinezmoreira.user_service.domain.dto.response.TokenResponse;
import com.hectormartinezmoreira.user_service.domain.exception.ErrorMessageException;
import com.hectormartinezmoreira.user_service.persistence.model.User;
import com.hectormartinezmoreira.user_service.persistence.repository.UserRepository;
import com.hectormartinezmoreira.user_service.web.config.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ErrorMessageException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ErrorMessageException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        return ResponseEntity.ok(TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration().getTime())
                .tokenType("Bearer")
                .build());
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader(name = "Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
        if (accessToken.isEmpty()) {
            throw new ErrorMessageException("Access token not present in headers", HttpStatus.UNAUTHORIZED);
        }

        try {
            String email = jwtUtil.extractUsername(accessToken);

            // Verify user still exists
            if (userRepository.findByEmail(email).isEmpty()) {
                throw new ErrorMessageException("User not found", HttpStatus.NOT_FOUND);
            }

            String newAccessToken = jwtUtil.generateAccessToken(email);
            String newRefreshToken = jwtUtil.generateRefreshToken(email);

            return ResponseEntity.ok(TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .expiresIn(jwtUtil.getAccessTokenExpiration().getTime())
                    .tokenType("Bearer")
                    .build());
        } catch (JwtException ex) {
            throw new ErrorMessageException(INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }
}
