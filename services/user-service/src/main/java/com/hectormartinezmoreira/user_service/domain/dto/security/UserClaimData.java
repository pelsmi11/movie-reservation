package com.hectormartinezmoreira.user_service.domain.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserClaimData {
    UUID idUser;
    List<String> roles;
    String email;
}
