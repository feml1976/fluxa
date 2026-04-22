package com.fml.fluxa.commitment.infrastructure.persistence;

import com.fml.fluxa.commitment.domain.model.CommitmentRecord;
import com.fml.fluxa.commitment.domain.model.CommitmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CommitmentRecordJpaRepository extends JpaRepository<CommitmentRecord, Long> {

    List<CommitmentRecord> findByUserIdAndPeriodMonthAndPeriodYear(Long userId, int month, int year);

    Optional<CommitmentRecord> findByCommitmentIdAndPeriodMonthAndPeriodYear(Long commitmentId, int month, int year);

    Optional<CommitmentRecord> findByIdAndUserId(Long id, Long userId);

    List<CommitmentRecord> findByUserIdAndStatus(Long userId, CommitmentStatus status);

    @Query("SELECT COALESCE(SUM(r.estimatedAmount), 0) FROM CommitmentRecord r " +
           "WHERE r.userId = :userId AND r.periodMonth = :month AND r.periodYear = :year")
    BigDecimal sumEstimatedByPeriod(Long userId, int month, int year);

    @Query("SELECT COALESCE(SUM(r.actualAmount), 0) FROM CommitmentRecord r " +
           "WHERE r.userId = :userId AND r.periodMonth = :month AND r.periodYear = :year " +
           "AND r.status = com.fml.fluxa.commitment.domain.model.CommitmentStatus.PAID")
    BigDecimal sumPaidByPeriod(Long userId, int month, int year);

    @Query("SELECT r FROM CommitmentRecord r WHERE r.userId = :userId " +
           "AND r.status = com.fml.fluxa.commitment.domain.model.CommitmentStatus.PENDING " +
           "AND r.dueDate BETWEEN :from AND :to ORDER BY r.dueDate ASC")
    List<CommitmentRecord> findUpcomingPending(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
