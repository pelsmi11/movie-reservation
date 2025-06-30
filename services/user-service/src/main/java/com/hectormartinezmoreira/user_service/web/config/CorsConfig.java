package com.hectormartinezmoreira.user_service.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {
    /**
     * The bean that provides a {@link CorsConfigurationSource} to be used to configure
     * Cross-Origin Resource Sharing (CORS) for the application.
     * <p>
     * The configuration allows all origins, GET, POST, PUT, DELETE, and OPTIONS methods,
     * and allows all headers.
     * <p>
     * The configuration also allows credentials (cookies, auth headers, etc.) to be
     * included in the request.
     * <p>
     * The configuration is applied to all URLs in the application.
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*")); // Allow all origins
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // If you're using cookies or auth headers

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
