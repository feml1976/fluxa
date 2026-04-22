package com.fml.fluxa.credit.infrastructure.persistence;

import com.fml.fluxa.credit.domain.model.Credit;
import com.fml.fluxa.credit.domain.model.CreditStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CreditJpaRepository extends JpaRepository<Credit, Long> {

    List<Credit> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long userId);

    List<Credit> findByUserIdAndStatusAndDeletedAtIsNull(Long userId, CreditStatus status);

    Optional<Credit> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);

    @Query("SELECT COALESCE(SUM(c.currentBalance), 0) FROM Credit c " +
           "WHERE c.userId = :userId AND c.status = 'ACTIVE' AND c.deletedAt IS NULL")
    BigDecimal sumActiveBalanceByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(c.monthlyInstallment), 0) FROM Credit c " +
           "WHERE c.userId = :userId AND c.status = 'ACTIVE' AND c.deletedAt IS NULL " +
           "AND c.type <> com.fml.fluxa.credit.domain.model.CreditType.CREDIT_CARD")
    BigDecimal sumMonthlyInstallmentsByUser(@Param("userId") Long userId);
}
