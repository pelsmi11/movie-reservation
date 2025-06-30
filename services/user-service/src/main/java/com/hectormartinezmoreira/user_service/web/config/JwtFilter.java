package com.hectormartinezmoreira.user_service.web.config;

import com.hectormartinezmoreira.user_service.domain.dto.security.UserClaimData;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //1. validate should be a validate header autoriation
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String requestPath = request.getRequestURI();

        // Skip filtering for login or refresh endpoints:
        if (requestPath.contains("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (authHeader == null || !authHeader.startsWith("Bearer")){
            filterChain.doFilter(request,response);
            return;
        }

        String jwt =  authHeader.split(" ")[1].trim();


        // this part is for refresh token
        if (requestPath.contains("/api/auth/refresh")) {
            boolean isValidRefreshToken = this.jwtUtil.isValidRefreshToken(jwt);
            if (!isValidRefreshToken) {
                filterChain.doFilter(request, response);
                return;
            }
            UUID userId = this.jwtUtil.extractUserIdFromRefreshToken(jwt);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of()
                    );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
            return;
        }

        // this part is for access token
        //2. jwt should be valid
        if (!this.jwtUtil.isValidAccessToken(jwt)){
            filterChain.doFilter(request,response);
            return;
        }
        //3. charge user of UserDetailService
        UserClaimData userClaimData =this.jwtUtil.extractUserClaimDataFromAccessToken(jwt);
        List<GrantedAuthority> authorities = userClaimData.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userClaimData.getIdUser(),
                        null,
                        authorities
                );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        System.out.println(authenticationToken);
        filterChain.doFilter(request,response);
    }
}
