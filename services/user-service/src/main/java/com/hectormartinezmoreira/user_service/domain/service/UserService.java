package com.hectormartinezmoreira.user_service.domain.service;

import com.hectormartinezmoreira.user_service.domain.dto.request.UserRequestDTO;
import com.hectormartinezmoreira.user_service.domain.dto.response.UserResponseDTO;
import com.hectormartinezmoreira.user_service.persistence.model.Role;
import com.hectormartinezmoreira.user_service.persistence.model.UserEntity;
import com.hectormartinezmoreira.user_service.persistence.model.UserRole;
import com.hectormartinezmoreira.user_service.persistence.model.UserRoleId;
import com.hectormartinezmoreira.user_service.persistence.repository.RoleRepository;
import com.hectormartinezmoreira.user_service.persistence.repository.UserRepository;
import com.hectormartinezmoreira.user_service.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user given a {@link UserRequestDTO} containing the desired user credentials.
     * The password in the request is hashed using the configured {@link PasswordEncoder} before
     * being saved to the database.
     * <p>
     * The default role for the user is set to "USER", which is looked up in the database using
     * the {@link RoleRepository}.
     * <p>
     * If the default role is not found, a {@link RuntimeException} is thrown.
     * <p>
     * The newly created user is returned as a {@link UserResponseDTO} containing the user's
     * identifier, username, and email.
     *
     * @param dto user credentials
     * @return the newly created user
     */
    public UserResponseDTO createUser(UserRequestDTO dto) {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());

        // 🔐 Hash the password
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 🎯 Set default role
        Role defaultRole = roleRepository.findByName("USER".toUpperCase())
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));

        user = userRepository.save(user);

        // 🎯 Set default role
        UserRoleId userRoleId = new UserRoleId(user.getId(), defaultRole.getId());
        UserRole userRole = UserRole.builder()
                .id(userRoleId)
                .user(user)
                .role(defaultRole)
                .build();

        userRoleRepository.save(userRole);
        return new UserResponseDTO(user.getId().toString(), user.getUsername(), user.getEmail());
    }

    public List<UserResponseDTO> getAllUsers() {
        List<UserEntity> users = (List<UserEntity>) userRepository.findAll();
        return users.stream()
                .map(user -> UserResponseDTO.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}
