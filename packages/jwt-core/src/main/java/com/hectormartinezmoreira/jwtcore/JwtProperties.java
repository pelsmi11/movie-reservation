package com.hectormartinezmoreira.jwtcore;

import lombok.Data;

@Data
public class JwtProperties {
    private String accessSecret;
    private String refreshSecret;
    private long accessExpiration = 15 * 60 * 1000; // 15 min
    private long refreshExpiration = 7 * 24 * 60 * 60 * 1000; // 7 days
}
