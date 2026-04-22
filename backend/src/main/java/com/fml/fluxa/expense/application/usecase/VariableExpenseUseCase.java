package com.fml.fluxa.expense.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fml.fluxa.expense.application.dto.VariableExpenseRequest;
import com.fml.fluxa.expense.application.dto.VariableExpenseResponse;
import com.fml.fluxa.expense.domain.model.VariableExpense;
import com.fml.fluxa.expense.infrastructure.persistence.ExpenseCategoryJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.VariableExpenseJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VariableExpenseUseCase {

    private final VariableExpenseJpaRepository expenseRepo;
    private final ExpenseCategoryJpaRepository categoryRepo;
    private final ObjectMapper objectMapper;

    public VariableExpenseUseCase(VariableExpenseJpaRepository expenseRepo,
                                  ExpenseCategoryJpaRepository categoryRepo,
                                  ObjectMapper objectMapper) {
        this.expenseRepo = expenseRepo;
        this.categoryRepo = categoryRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public VariableExpenseResponse create(Long userId, VariableExpenseRequest req) {
        String tagsJson = serializeTags(req.tags());
        VariableExpense expense = VariableExpense.builder()
                .userId(userId)
                .categoryId(req.categoryId())
                .amount(req.amount())
                .expenseDate(req.expenseDate())
                .description(req.description())
                .tags(tagsJson)
                .receiptUrl(req.receiptUrl())
                .build();
        VariableExpense saved = expenseRepo.save(expense);
        String catName = resolveCategoryName(userId, saved.getCategoryId());
        return VariableExpenseResponse.from(saved, catName);
    }

    @Transactional
    public VariableExpenseResponse update(Long userId, Long id, VariableExpenseRequest req) {
        VariableExpense expense = expenseRepo.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));
        expense.setCategoryId(req.categoryId());
        expense.setAmount(req.amount());
        expense.setExpenseDate(req.expenseDate());
        expense.setDescription(req.description());
        expense.setTags(serializeTags(req.tags()));
        expense.setReceiptUrl(req.receiptUrl());
        VariableExpense saved = expenseRepo.save(expense);
        String catName = resolveCategoryName(userId, saved.getCategoryId());
        return VariableExpenseResponse.from(saved, catName);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        VariableExpense expense = expenseRepo.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));
        expense.setDeletedAt(Instant.now());
        expense.setDeletedBy(userId);
        expenseRepo.save(expense);
    }

    @Transactional(readOnly = true)
    public List<VariableExpenseResponse> listByPeriod(Long userId, int month, int year) {
        var start = java.time.LocalDate.of(year, month, 1);
        var end = start.withDayOfMonth(start.lengthOfMonth());
        var expenses = expenseRepo
                .findByUserIdAndExpenseDateBetweenAndDeletedAtIsNullOrderByExpenseDateDesc(userId, start, end);

        Map<Long, String> catNames = buildCategoryNameMap(userId);
        return expenses.stream()
                .map(e -> VariableExpenseResponse.from(e, catNames.getOrDefault(e.getCategoryId(), "Sin categoría")))
                .toList();
    }

    private String resolveCategoryName(Long userId, Long categoryId) {
        return categoryRepo.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)
                .map(c -> c.getName())
                .orElse("Sin categoría");
    }

    private Map<Long, String> buildCategoryNameMap(Long userId) {
        return categoryRepo.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream()
                .collect(Collectors.toMap(c -> c.getId(), c -> c.getName()));
    }

    private String serializeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            return null;
        }
    }
}
