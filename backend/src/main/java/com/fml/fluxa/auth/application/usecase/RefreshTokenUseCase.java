package com.fml.fluxa.auth.application.usecase;

import com.fml.fluxa.auth.application.dto.LoginResponse;
import com.fml.fluxa.auth.application.dto.UserResponse;
import com.fml.fluxa.auth.domain.model.RefreshToken;
import com.fml.fluxa.auth.infrastructure.config.JwtService;
import com.fml.fluxa.auth.infrastructure.persistence.RefreshTokenJpaRepository;
import com.fml.fluxa.shared.domain.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenUseCase {

    private final RefreshTokenJpaRepository refreshTokenRepository;
    private final JwtService jwtService;

    public RefreshTokenUseCase(RefreshTokenJpaRepository refreshTokenRepository,
                                JwtService jwtService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse execute(String rawRefreshToken) {
        RefreshToken token = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(rawRefreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido o revocado"));

        if (token.isExpired()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new UnauthorizedException("Refresh token expirado");
        }

        var user = token.getUser();
        String newAccessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());

        return LoginResponse.of(
                newAccessToken,
                rawRefreshToken,
                jwtService.getAccessTokenExpirationMs() / 1000,
                UserResponse.from(user)
        );
    }
}
