package com.fml.fluxa.income.application.usecase;

import com.fml.fluxa.income.application.dto.IncomeRecordResponse;
import com.fml.fluxa.income.application.dto.MonthlyIncomeSummary;
import com.fml.fluxa.income.application.dto.UpdateIncomeRecordRequest;
import com.fml.fluxa.income.domain.model.*;
import com.fml.fluxa.income.infrastructure.persistence.IncomeRecordJpaRepository;
import com.fml.fluxa.income.infrastructure.persistence.IncomeSourceJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MonthlyIncomeUseCase {

    private final IncomeSourceJpaRepository sourceRepository;
    private final IncomeRecordJpaRepository recordRepository;

    public MonthlyIncomeUseCase(IncomeSourceJpaRepository sourceRepository,
                                 IncomeRecordJpaRepository recordRepository) {
        this.sourceRepository = sourceRepository;
        this.recordRepository = recordRepository;
    }

    /**
     * Retorna los registros del período. Auto-genera registros EXPECTED para fuentes FIXED
     * que aún no tienen registro en ese mes/año.
     */
    @Transactional
    public MonthlyIncomeSummary getMonthly(Long userId, int month, int year) {
        // Auto-generar registros para fuentes fijas activas sin registro en el período
        List<IncomeSource> fixedSources = sourceRepository
                .findByUserIdAndTypeAndIsActiveTrueAndDeletedAtIsNull(userId, IncomeType.FIXED);

        for (IncomeSource src : fixedSources) {
            boolean exists = recordRepository
                    .findBySourceIdAndPeriodMonthAndPeriodYear(src.getId(), month, year)
                    .isPresent();
            if (!exists) {
                recordRepository.save(IncomeRecord.builder()
                        .userId(userId)
                        .sourceId(src.getId())
                        .amount(src.getExpectedAmount())
                        .periodMonth(month)
                        .periodYear(year)
                        .status(IncomeStatus.EXPECTED)
                        .build());
            }
        }

        // Mapeo de fuentes para enriquecer con nombre
        Map<Long, String> sourceNames = sourceRepository
                .findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream().collect(Collectors.toMap(IncomeSource::getId, IncomeSource::getName));

        List<IncomeRecordResponse> records = recordRepository
                .findByUserIdAndPeriodMonthAndPeriodYear(userId, month, year)
                .stream()
                .map(r -> IncomeRecordResponse.from(r, sourceNames.getOrDefault(r.getSourceId(), "—")))
                .toList();

        BigDecimal totalExpected = recordRepository.sumExpectedByPeriod(userId, month, year);
        BigDecimal totalReceived = recordRepository.sumReceivedByPeriod(userId, month, year);

        return new MonthlyIncomeSummary(month, year, totalExpected, totalReceived, records);
    }

    @Transactional
    public IncomeRecordResponse updateRecord(Long userId, Long recordId, UpdateIncomeRecordRequest req) {
        IncomeRecord record = recordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de ingreso no encontrado"));

        record.setAmount(req.amount());
        record.setStatus(req.status());
        record.setReceivedDate(req.receivedDate());
        record.setNotes(req.notes());

        IncomeRecord saved = recordRepository.save(record);
        String sourceName = sourceRepository.findById(saved.getSourceId())
                .map(IncomeSource::getName).orElse("—");
        return IncomeRecordResponse.from(saved, sourceName);
    }
}
