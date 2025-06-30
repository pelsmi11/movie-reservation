package com.hectormartinezmoreira.user_service.web.controller;

import com.hectormartinezmoreira.user_service.domain.dto.request.LoginRequest;
import com.hectormartinezmoreira.user_service.domain.dto.request.RefreshTokenRequest;
import com.hectormartinezmoreira.user_service.domain.dto.response.TokenResponse;
import com.hectormartinezmoreira.user_service.domain.exception.ErrorMessageException;
import com.hectormartinezmoreira.user_service.persistence.model.UserEntity;
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
import java.util.UUID;

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
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ErrorMessageException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            System.out.println("Invalid password");
            throw new ErrorMessageException(INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return ResponseEntity.ok(TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration().getTime())
                .tokenType("Bearer")
                .build());
    }

    @GetMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestHeader(name = "Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.substring(7); // Remove "Bearer " prefix
        if (refreshToken.isEmpty()) {
            throw new ErrorMessageException("Access token not present in headers", HttpStatus.UNAUTHORIZED);
        }

        try {
            UUID userId = jwtUtil.extractUserIdFromRefreshToken(refreshToken);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ErrorMessageException(INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED));

            String newAccestonToken = jwtUtil.generateAccessToken(user);

            return ResponseEntity.ok(TokenResponse.builder()
                    .accessToken(newAccestonToken)
                    .refreshToken(refreshToken)
                    .expiresIn(jwtUtil.getAccessTokenExpiration().getTime())
                    .tokenType("Bearer")
                    .build());
        } catch (JwtException ex) {
            throw new ErrorMessageException(INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
        }
    }
}
