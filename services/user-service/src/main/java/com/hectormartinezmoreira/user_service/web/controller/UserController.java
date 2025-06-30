package com.hectormartinezmoreira.user_service.web.controller;

import com.hectormartinezmoreira.user_service.domain.dto.request.UserRequestDTO;
import com.hectormartinezmoreira.user_service.domain.dto.response.UserResponseDTO;
import com.hectormartinezmoreira.user_service.domain.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO request) {
        UserResponseDTO user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
}
