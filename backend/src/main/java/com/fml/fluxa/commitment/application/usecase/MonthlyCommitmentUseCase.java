package com.fml.fluxa.commitment.application.usecase;

import com.fml.fluxa.commitment.application.dto.CommitmentRecordResponse;
import com.fml.fluxa.commitment.application.dto.MonthlyCommitmentSummary;
import com.fml.fluxa.commitment.application.dto.RegisterPaymentRequest;
import com.fml.fluxa.commitment.domain.model.*;
import com.fml.fluxa.commitment.infrastructure.persistence.CommitmentRecordJpaRepository;
import com.fml.fluxa.commitment.infrastructure.persistence.FixedCommitmentJpaRepository;
import com.fml.fluxa.shared.domain.exception.BusinessException;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MonthlyCommitmentUseCase {

    private final FixedCommitmentJpaRepository commitmentRepository;
    private final CommitmentRecordJpaRepository recordRepository;

    public MonthlyCommitmentUseCase(FixedCommitmentJpaRepository commitmentRepository,
                                     CommitmentRecordJpaRepository recordRepository) {
        this.commitmentRepository = commitmentRepository;
        this.recordRepository = recordRepository;
    }

    /**
     * Retorna el resumen del período. Auto-genera registros PENDING para compromisos activos
     * sin registro en ese mes/año y actualiza los VENCIDOS.
     */
    @Transactional
    public MonthlyCommitmentSummary getMonthly(Long userId, int month, int year) {
        List<FixedCommitment> active = commitmentRepository.findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);

        for (FixedCommitment c : active) {
            boolean exists = recordRepository
                    .findByCommitmentIdAndPeriodMonthAndPeriodYear(c.getId(), month, year)
                    .isPresent();
            if (!exists) {
                LocalDate dueDate = calculateDueDate(year, month, c.getDueDay());
                recordRepository.save(CommitmentRecord.builder()
                        .userId(userId)
                        .commitmentId(c.getId())
                        .periodMonth(month)
                        .periodYear(year)
                        .estimatedAmount(c.getEstimatedAmount())
                        .dueDate(dueDate)
                        .status(CommitmentStatus.PENDING)
                        .build());
            }
        }

        // Marcar como OVERDUE los pendientes con fecha vencida
        List<CommitmentRecord> allRecords = recordRepository
                .findByUserIdAndPeriodMonthAndPeriodYear(userId, month, year);

        LocalDate today = LocalDate.now();
        for (CommitmentRecord r : allRecords) {
            if (r.getStatus() == CommitmentStatus.PENDING && r.getDueDate().isBefore(today)) {
                r.setStatus(CommitmentStatus.OVERDUE);
                recordRepository.save(r);
            }
        }

        Map<Long, String> names = commitmentRepository
                .findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream().collect(Collectors.toMap(FixedCommitment::getId, FixedCommitment::getName));

        List<CommitmentRecordResponse> records = recordRepository
                .findByUserIdAndPeriodMonthAndPeriodYear(userId, month, year)
                .stream()
                .map(r -> CommitmentRecordResponse.from(r, names.getOrDefault(r.getCommitmentId(), "—")))
                .toList();

        BigDecimal totalEstimated = recordRepository.sumEstimatedByPeriod(userId, month, year);
        BigDecimal totalPaid      = recordRepository.sumPaidByPeriod(userId, month, year);

        long pending  = records.stream().filter(r -> r.status() == CommitmentStatus.PENDING).count();
        long paid     = records.stream().filter(r -> r.status() == CommitmentStatus.PAID).count();
        long overdue  = records.stream().filter(r -> r.status() == CommitmentStatus.OVERDUE).count();

        return new MonthlyCommitmentSummary(month, year, totalEstimated, totalPaid,
                pending, paid, overdue, records);
    }

    @Transactional
    public CommitmentRecordResponse registerPayment(Long userId, Long commitmentId,
                                                     int month, int year,
                                                     RegisterPaymentRequest req) {
        FixedCommitment commitment = commitmentRepository
                .findByIdAndUserIdAndDeletedAtIsNull(commitmentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Compromiso no encontrado"));

        CommitmentRecord record = recordRepository
                .findByCommitmentIdAndPeriodMonthAndPeriodYear(commitmentId, month, year)
                .orElseGet(() -> {
                    LocalDate dueDate = calculateDueDate(year, month, commitment.getDueDay());
                    return CommitmentRecord.builder()
                            .userId(userId)
                            .commitmentId(commitmentId)
                            .periodMonth(month)
                            .periodYear(year)
                            .estimatedAmount(commitment.getEstimatedAmount())
                            .dueDate(dueDate)
                            .status(CommitmentStatus.PENDING)
                            .build();
                });

        if (record.getStatus() == CommitmentStatus.PAID) {
            throw new BusinessException("Este compromiso ya fue pagado en el período indicado", "ALREADY_PAID");
        }

        record.setActualAmount(req.actualAmount());
        record.setPaidDate(req.paidDate() != null ? req.paidDate() : LocalDate.now());
        record.setReceiptReference(req.receiptReference());
        record.setNotes(req.notes());
        record.setStatus(CommitmentStatus.PAID);

        CommitmentRecord saved = recordRepository.save(record);
        return CommitmentRecordResponse.from(saved, commitment.getName());
    }

    private LocalDate calculateDueDate(int year, int month, int dueDay) {
        YearMonth ym = YearMonth.of(year, month);
        int lastDay = ym.lengthOfMonth();
        return LocalDate.of(year, month, Math.min(dueDay, lastDay));
    }
}
