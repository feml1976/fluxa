package com.fml.fluxa.commitment.infrastructure.persistence;

import com.fml.fluxa.commitment.domain.model.FixedCommitment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FixedCommitmentJpaRepository extends JpaRepository<FixedCommitment, Long> {

    List<FixedCommitment> findByUserIdAndDeletedAtIsNullOrderByNameAsc(Long userId);

    List<FixedCommitment> findByUserIdAndIsActiveTrueAndDeletedAtIsNull(Long userId);

    Optional<FixedCommitment> findByIdAndUserIdAndDeletedAtIsNull(Long id, Long userId);
}
