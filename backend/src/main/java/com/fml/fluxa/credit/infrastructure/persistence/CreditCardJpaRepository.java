package com.fml.fluxa.credit.infrastructure.persistence;

import com.fml.fluxa.credit.domain.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CreditCardJpaRepository extends JpaRepository<CreditCard, Long> {

    Optional<CreditCard> findByCreditId(Long creditId);

    List<CreditCard> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(cc.creditLimitPurchases - cc.availablePurchases), 0) " +
           "FROM CreditCard cc WHERE cc.userId = :userId")
    BigDecimal sumUsedBalanceByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(cc.minimumPayment), 0) FROM CreditCard cc WHERE cc.userId = :userId")
    BigDecimal sumMinimumPaymentsByUser(@Param("userId") Long userId);
}
