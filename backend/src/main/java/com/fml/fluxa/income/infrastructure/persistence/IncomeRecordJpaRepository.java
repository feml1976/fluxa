package com.fml.fluxa.income.infrastructure.persistence;

import com.fml.fluxa.income.domain.model.IncomeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IncomeRecordJpaRepository extends JpaRepository<IncomeRecord, Long> {

    List<IncomeRecord> findByUserIdAndPeriodMonthAndPeriodYear(Long userId, int month, int year);

    Optional<IncomeRecord> findBySourceIdAndPeriodMonthAndPeriodYear(Long sourceId, int month, int year);

    Optional<IncomeRecord> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM IncomeRecord r " +
           "WHERE r.userId = :userId AND r.periodMonth = :month AND r.periodYear = :year " +
           "AND r.status IN (com.fml.fluxa.income.domain.model.IncomeStatus.RECEIVED, " +
           "                 com.fml.fluxa.income.domain.model.IncomeStatus.PARTIAL)")
    BigDecimal sumReceivedByPeriod(Long userId, int month, int year);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM IncomeRecord r " +
           "WHERE r.userId = :userId AND r.periodMonth = :month AND r.periodYear = :year")
    BigDecimal sumExpectedByPeriod(Long userId, int month, int year);
}
