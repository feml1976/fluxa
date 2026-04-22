package com.fml.fluxa.credit.infrastructure.persistence;

import com.fml.fluxa.credit.domain.model.CreditPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditPaymentJpaRepository extends JpaRepository<CreditPayment, Long> {

    List<CreditPayment> findByCreditIdOrderByPeriodYearDescPeriodMonthDesc(Long creditId);

    Optional<CreditPayment> findByCreditIdAndPeriodMonthAndPeriodYear(Long creditId, int month, int year);

    List<CreditPayment> findByUserIdAndPeriodMonthAndPeriodYear(Long userId, int month, int year);
}
