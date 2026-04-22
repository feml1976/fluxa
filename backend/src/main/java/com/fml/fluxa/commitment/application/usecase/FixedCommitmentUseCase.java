package com.fml.fluxa.commitment.application.usecase;

import com.fml.fluxa.commitment.application.dto.FixedCommitmentRequest;
import com.fml.fluxa.commitment.application.dto.FixedCommitmentResponse;
import com.fml.fluxa.commitment.domain.model.FixedCommitment;
import com.fml.fluxa.commitment.infrastructure.persistence.FixedCommitmentJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class FixedCommitmentUseCase {

    private final FixedCommitmentJpaRepository repository;

    public FixedCommitmentUseCase(FixedCommitmentJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FixedCommitmentResponse create(Long userId, FixedCommitmentRequest req) {
        FixedCommitment commitment = FixedCommitment.builder()
                .userId(userId)
                .categoryId(req.categoryId())
                .name(req.name().trim())
                .description(req.description())
                .estimatedAmount(req.estimatedAmount())
                .dueDay(req.dueDay())
                .frequency(req.frequency())
                .alertDaysBefore(req.alertDaysBefore() != null ? req.alertDaysBefore() : 5)
                .isActive(true)
                .build();
        return FixedCommitmentResponse.from(repository.save(commitment));
    }

    @Transactional(readOnly = true)
    public List<FixedCommitmentResponse> listByUser(Long userId) {
        return repository.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream().map(FixedCommitmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public FixedCommitmentResponse getById(Long userId, Long id) {
        return repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(FixedCommitmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Compromiso no encontrado"));
    }

    @Transactional
    public FixedCommitmentResponse update(Long userId, Long id, FixedCommitmentRequest req) {
        FixedCommitment commitment = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Compromiso no encontrado"));
        commitment.setCategoryId(req.categoryId());
        commitment.setName(req.name().trim());
        commitment.setDescription(req.description());
        commitment.setEstimatedAmount(req.estimatedAmount());
        commitment.setDueDay(req.dueDay());
        commitment.setFrequency(req.frequency());
        if (req.alertDaysBefore() != null) commitment.setAlertDaysBefore(req.alertDaysBefore());
        return FixedCommitmentResponse.from(repository.save(commitment));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        FixedCommitment commitment = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Compromiso no encontrado"));
        commitment.setDeletedAt(Instant.now());
        commitment.setDeletedBy(userId);
        repository.save(commitment);
    }
}
