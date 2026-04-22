package com.fml.fluxa.expense.infrastructure.persistence;

import com.fml.fluxa.expense.domain.model.VariableExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VariableExpenseJpaRepository extends JpaRepository<VariableExpense, Long> {

    List<VariableExpense> findByUserIdAndExpenseDateBetweenAndDeletedAtIsNullOrderByExpenseDateDesc(
            Long userId, LocalDate from, LocalDate to);

    Optional<VariableExpense> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query(value = """
            SELECT category_id AS categoryId, SUM(amount) AS total
            FROM variable_expenses
            WHERE user_id = :userId
              AND EXTRACT(MONTH FROM expense_date) = :month
              AND EXTRACT(YEAR  FROM expense_date) = :year
              AND deleted_at IS NULL
            GROUP BY category_id
            ORDER BY total DESC
            LIMIT 5
            """, nativeQuery = true)
    List<CategoryExpenseProjection> findTop5CategoriesByPeriod(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM variable_expenses
            WHERE user_id = :userId
              AND EXTRACT(MONTH FROM expense_date) = :month
              AND EXTRACT(YEAR  FROM expense_date) = :year
              AND deleted_at IS NULL
            """, nativeQuery = true)
    BigDecimal sumByUserAndPeriod(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM variable_expenses
            WHERE user_id = :userId
              AND category_id = :categoryId
              AND EXTRACT(MONTH FROM expense_date) = :month
              AND EXTRACT(YEAR  FROM expense_date) = :year
              AND deleted_at IS NULL
            """, nativeQuery = true)
    BigDecimal sumByUserCategoryAndPeriod(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("month") int month,
            @Param("year") int year);
}
