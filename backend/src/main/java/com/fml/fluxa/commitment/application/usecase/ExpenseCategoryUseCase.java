package com.fml.fluxa.commitment.application.usecase;

import com.fml.fluxa.commitment.application.dto.ExpenseCategoryRequest;
import com.fml.fluxa.commitment.application.dto.ExpenseCategoryResponse;
import com.fml.fluxa.expense.domain.model.ExpenseCategory;
import com.fml.fluxa.expense.domain.model.ExpenseCategoryType;
import com.fml.fluxa.expense.infrastructure.persistence.ExpenseCategoryJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;

@Service
public class ExpenseCategoryUseCase {

    private final ExpenseCategoryJpaRepository repository;

    public ExpenseCategoryUseCase(ExpenseCategoryJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ExpenseCategoryResponse create(Long userId, ExpenseCategoryRequest req) {
        ExpenseCategory cat = ExpenseCategory.builder()
                .userId(userId)
                .name(req.name().trim())
                .color(req.color() != null ? req.color() : "#d32f2f")
                .icon(req.icon())
                .type(req.type() != null ? req.type() : ExpenseCategoryType.FIXED)
                .build();
        return ExpenseCategoryResponse.from(repository.save(cat));
    }

    @Transactional(readOnly = true)
    public List<ExpenseCategoryResponse> listByUser(Long userId, ExpenseCategoryType type) {
        List<ExpenseCategory> cats = (type != null)
                ? repository.findByUserIdAndTypeAndDeletedAtIsNullOrderByNameAsc(userId, type)
                : repository.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId);
        return cats.stream().map(ExpenseCategoryResponse::from).toList();
    }

    @Transactional
    public ExpenseCategoryResponse update(Long userId, Long id, ExpenseCategoryRequest req) {
        ExpenseCategory cat = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        cat.setName(req.name().trim());
        if (req.color() != null) cat.setColor(req.color());
        if (req.icon() != null) cat.setIcon(req.icon());
        if (req.type() != null) cat.setType(req.type());
        return ExpenseCategoryResponse.from(repository.save(cat));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        ExpenseCategory cat = repository.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
        cat.setDeletedAt(Instant.now());
        cat.setDeletedBy(userId);
        repository.save(cat);
    }
}
