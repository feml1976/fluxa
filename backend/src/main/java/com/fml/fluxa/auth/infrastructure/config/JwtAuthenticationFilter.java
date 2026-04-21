package com.fml.fluxa.auth.infrastructure.config;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.auth.infrastructure.persistence.UserJpaRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserJpaRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserJpaRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.validateAndExtractClaims(token);
            Long userId = Long.parseLong(claims.getSubject());

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
                if (user != null && user.isActive()) {
                    String role = claims.get("role", String.class);
                    var auth = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (JwtException | IllegalArgumentException ignored) {
            // Token inválido — continúa sin autenticar; Spring Security rechazará si el endpoint lo requiere
        }

        filterChain.doFilter(request, response);
    }
}
