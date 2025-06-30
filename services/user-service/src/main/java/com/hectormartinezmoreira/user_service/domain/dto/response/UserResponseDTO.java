package com.hectormartinezmoreira.user_service.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
}
