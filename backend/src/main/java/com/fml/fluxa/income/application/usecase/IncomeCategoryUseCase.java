package com.fml.fluxa.income.application.usecase;

import com.fml.fluxa.income.application.dto.IncomeCategoryRequest;
import com.fml.fluxa.income.application.dto.IncomeCategoryResponse;
import com.fml.fluxa.income.domain.model.IncomeCategory;
import com.fml.fluxa.income.infrastructure.persistence.IncomeCategoryJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class IncomeCategoryUseCase {

    private final IncomeCategoryJpaRepository repository;

    public IncomeCategoryUseCase(IncomeCategoryJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IncomeCategoryResponse create(Long userId, IncomeCategoryRequest req) {
        IncomeCategory cat = IncomeCategory.builder()
                .userId(userId)
                .name(req.name().trim())
                .color(req.color() != null ? req.color() : "#1976d2")
                .icon(req.icon())
                .build();
        return IncomeCategoryResponse.from(repository.save(cat));
    }

    @Transactional(readOnly = true)
    public List<IncomeCategoryResponse> listByUser(Long userId) {
        return repository.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream().map(IncomeCategoryResponse::from).toList();
    }

    @Transactional
    public IncomeCategoryResponse update(Long userId, Long id, IncomeCategoryRequest req) {
        IncomeCategory cat = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        cat.setName(req.name().trim());
        if (req.color() != null) cat.setColor(req.color());
        if (req.icon() != null) cat.setIcon(req.icon());
        return IncomeCategoryResponse.from(repository.save(cat));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        IncomeCategory cat = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        cat.setDeletedAt(Instant.now());
        cat.setDeletedBy(userId);
        repository.save(cat);
    }
}
