package com.fml.fluxa.auth.application.usecase;

import com.fml.fluxa.auth.application.dto.RegisterRequest;
import com.fml.fluxa.auth.application.dto.UserResponse;
import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.auth.domain.model.UserRole;
import com.fml.fluxa.auth.infrastructure.persistence.UserJpaRepository;
import com.fml.fluxa.shared.domain.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUseCase {

    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCase(UserJpaRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse execute(RegisterRequest request) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw new BusinessException("El email ya está registrado", "EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .email(request.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .role(UserRole.USER)
                .isActive(true)
                .build();

        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }
}
