package com.fml.fluxa.income.infrastructure.persistence;

import com.fml.fluxa.income.domain.model.IncomeSource;
import com.fml.fluxa.income.domain.model.IncomeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IncomeSourceJpaRepository extends JpaRepository<IncomeSource, Long> {

    List<IncomeSource> findByUserIdAndDeletedAtIsNullOrderByNameAsc(Long userId);

    List<IncomeSource> findByUserIdAndTypeAndIsActiveTrueAndDeletedAtIsNull(Long userId, IncomeType type);

    Optional<IncomeSource> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
