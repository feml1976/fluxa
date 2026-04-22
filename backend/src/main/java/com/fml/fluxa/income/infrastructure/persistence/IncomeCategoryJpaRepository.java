package com.fml.fluxa.income.infrastructure.persistence;

import com.fml.fluxa.income.domain.model.IncomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface IncomeCategoryJpaRepository extends JpaRepository<IncomeCategory, Long> {

    List<IncomeCategory> findByUserIdAndDeletedAtIsNullOrderByNameAsc(Long userId);

    Optional<IncomeCategory> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
