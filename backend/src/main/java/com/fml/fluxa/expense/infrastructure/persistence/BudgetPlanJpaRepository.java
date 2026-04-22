package com.fml.fluxa.expense.infrastructure.persistence;

import com.fml.fluxa.expense.domain.model.BudgetPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface BudgetPlanJpaRepository extends JpaRepository<BudgetPlan, Long> {

    List<BudgetPlan> findByUserIdAndPeriodMonthAndPeriodYear(Long userId, int month, int year);

    Optional<BudgetPlan> findByUserIdAndCategoryIdAndPeriodMonthAndPeriodYear(
            Long userId, Long categoryId, int month, int year);

    @Query("SELECT COALESCE(SUM(b.plannedAmount), 0) FROM BudgetPlan b " +
           "WHERE b.userId = :userId AND b.periodMonth = :month AND b.periodYear = :year")
    BigDecimal sumPlannedByPeriod(
            @Param("userId") Long userId,
            @Param("month") int month,
            @Param("year") int year);
}
