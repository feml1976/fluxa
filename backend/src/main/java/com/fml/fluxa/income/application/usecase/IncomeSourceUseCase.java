package com.fml.fluxa.income.application.usecase;

import com.fml.fluxa.income.application.dto.IncomeSourceRequest;
import com.fml.fluxa.income.application.dto.IncomeSourceResponse;
import com.fml.fluxa.income.domain.model.IncomeSource;
import com.fml.fluxa.income.infrastructure.persistence.IncomeSourceJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class IncomeSourceUseCase {

    private final IncomeSourceJpaRepository repository;

    public IncomeSourceUseCase(IncomeSourceJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IncomeSourceResponse create(Long userId, IncomeSourceRequest req) {
        IncomeSource source = IncomeSource.builder()
                .userId(userId)
                .categoryId(req.categoryId())
                .name(req.name().trim())
                .description(req.description())
                .type(req.type())
                .expectedAmount(req.expectedAmount())
                .frequency(req.frequency())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .isActive(true)
                .build();
        return IncomeSourceResponse.from(repository.save(source));
    }

    @Transactional(readOnly = true)
    public List<IncomeSourceResponse> listByUser(Long userId) {
        return repository.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream().map(IncomeSourceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public IncomeSourceResponse getById(Long userId, Long id) {
        return repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .map(IncomeSourceResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Fuente de ingreso no encontrada"));
    }

    @Transactional
    public IncomeSourceResponse update(Long userId, Long id, IncomeSourceRequest req) {
        IncomeSource source = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuente de ingreso no encontrada"));
        source.setCategoryId(req.categoryId());
        source.setName(req.name().trim());
        source.setDescription(req.description());
        source.setType(req.type());
        source.setExpectedAmount(req.expectedAmount());
        source.setFrequency(req.frequency());
        source.setStartDate(req.startDate());
        source.setEndDate(req.endDate());
        return IncomeSourceResponse.from(repository.save(source));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        IncomeSource source = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Fuente de ingreso no encontrada"));
        source.setDeletedAt(Instant.now());
        source.setDeletedBy(userId);
        repository.save(source);
    }
}
