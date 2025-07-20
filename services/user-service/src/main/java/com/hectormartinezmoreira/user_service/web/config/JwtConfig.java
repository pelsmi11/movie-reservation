package com.hectormartinezmoreira.user_service.web.config;

import com.hectormartinezmoreira.jwtcore.JwtFilter;
import com.hectormartinezmoreira.jwtcore.JwtProperties;
import com.hectormartinezmoreira.jwtcore.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {
    @Value("${movie-service.secret.access}")
    private String accessSecret;

    @Value("${movie-service.secret.refresh}")
    private String refreshSecret;

    @Bean
    public JwtProperties jwtProperties() {
        JwtProperties props = new JwtProperties();
        props.setAccessSecret(accessSecret);
        props.setRefreshSecret(refreshSecret);
        return props;
    }

    @Bean
    public JwtUtil jwtUtil(JwtProperties jwtProperties) {
        return new JwtUtil(jwtProperties);
    }

    @Bean
    public JwtFilter jwtFilter(JwtUtil jwtUtil) {
        return new JwtFilter(jwtUtil);
    }
}
