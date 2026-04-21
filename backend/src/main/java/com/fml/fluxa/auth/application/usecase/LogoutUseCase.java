package com.fml.fluxa.auth.application.usecase;

import com.fml.fluxa.auth.infrastructure.persistence.RefreshTokenJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutUseCase {

    private final RefreshTokenJpaRepository refreshTokenRepository;

    public LogoutUseCase(RefreshTokenJpaRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public void execute(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
