package com.fml.fluxa.auth.application.usecase;

import com.fml.fluxa.auth.application.dto.LoginRequest;
import com.fml.fluxa.auth.application.dto.LoginResponse;
import com.fml.fluxa.auth.application.dto.UserResponse;
import com.fml.fluxa.auth.domain.model.RefreshToken;
import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.auth.infrastructure.config.JwtService;
import com.fml.fluxa.auth.infrastructure.persistence.RefreshTokenJpaRepository;
import com.fml.fluxa.auth.infrastructure.persistence.UserJpaRepository;
import com.fml.fluxa.shared.domain.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class LoginUseCase {

    private final UserJpaRepository userRepository;
    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final long refreshTokenExpirationMs;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public LoginUseCase(
            UserJpaRepository userRepository,
            RefreshTokenJpaRepository refreshTokenRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            @Value("${fluxa.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public LoginResponse execute(LoginRequest request) {
        User user = userRepository
                .findByEmailAndDeletedAtIsNull(request.email().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Usuario inactivo");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        String rawRefreshToken = generateSecureToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(rawRefreshToken)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpirationMs))
                .build());

        return LoginResponse.of(
                accessToken,
                rawRefreshToken,
                jwtService.getAccessTokenExpirationMs() / 1000,
                UserResponse.from(user)
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[48];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
