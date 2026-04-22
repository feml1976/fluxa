package com.fml.fluxa.expense.infrastructure.persistence;

import com.fml.fluxa.expense.domain.model.ExpenseCategory;
import com.fml.fluxa.expense.domain.model.ExpenseCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryJpaRepository extends JpaRepository<ExpenseCategory, Long> {

    List<ExpenseCategory> findByUserIdAndDeletedAtIsNullOrderByNameAsc(Long userId);

    List<ExpenseCategory> findByUserIdAndTypeAndDeletedAtIsNullOrderByNameAsc(Long userId, ExpenseCategoryType type);

    Optional<ExpenseCategory> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
