package com.fml.fluxa.credit.application.usecase;

import com.fml.fluxa.credit.application.dto.*;
import com.fml.fluxa.credit.domain.model.*;
import com.fml.fluxa.credit.infrastructure.persistence.*;
import com.fml.fluxa.shared.domain.exception.BusinessException;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

@Service
public class CreditUseCase {

    private final CreditJpaRepository creditRepo;
    private final CreditCardJpaRepository cardRepo;
    private final CreditPaymentJpaRepository paymentRepo;
    private final CreditAnalysisUseCase analysisUseCase;

    public CreditUseCase(CreditJpaRepository creditRepo,
                         CreditCardJpaRepository cardRepo,
                         CreditPaymentJpaRepository paymentRepo,
                         CreditAnalysisUseCase analysisUseCase) {
        this.creditRepo = creditRepo;
        this.cardRepo = cardRepo;
        this.paymentRepo = paymentRepo;
        this.analysisUseCase = analysisUseCase;
    }

    @Transactional
    public CreditResponse create(Long userId, CreditRequest req) {
        if (req.type() == CreditType.CREDIT_CARD && req.cardDetail() == null) {
            throw new BusinessException("Los datos de tarjeta son obligatorios para tipo CREDIT_CARD");
        }

        Credit credit = Credit.builder()
                .userId(userId)
                .type(req.type())
                .status(req.status() != null ? req.status() : CreditStatus.ACTIVE)
                .name(req.name().trim())
                .description(req.description())
                .interestRateMv(req.interestRateMv())
                .currentBalance(req.currentBalance())
                .monthlyInstallment(req.monthlyInstallment())
                .totalInstallments(req.totalInstallments())
                .paidInstallments(req.paidInstallments() != null ? req.paidInstallments() : 0)
                .openingDate(req.openingDate())
                .closingDate(req.closingDate())
                .build();
        credit = creditRepo.save(credit);

        CreditCard savedCard = null;
        if (req.type() == CreditType.CREDIT_CARD) {
            savedCard = saveCardDetail(credit.getId(), userId, req.cardDetail());
        }

        CreditCardDetailResponse cardDto = savedCard != null ? CreditCardDetailResponse.from(savedCard) : null;
        BigDecimal ea = calculateEa(credit.getInterestRateMv());
        CreditAlertLevel alert = analysisUseCase.resolveAlertLevel(credit, savedCard);
        return CreditResponse.from(credit, cardDto, ea, alert);
    }

    @Transactional
    public CreditResponse update(Long userId, Long id, CreditRequest req) {
        Credit credit = getOwned(id, userId);

        credit.setType(req.type());
        if (req.status() != null) credit.setStatus(req.status());
        credit.setName(req.name().trim());
        credit.setDescription(req.description());
        credit.setInterestRateMv(req.interestRateMv());
        credit.setCurrentBalance(req.currentBalance());
        credit.setMonthlyInstallment(req.monthlyInstallment());
        credit.setTotalInstallments(req.totalInstallments());
        if (req.paidInstallments() != null) credit.setPaidInstallments(req.paidInstallments());
        credit.setOpeningDate(req.openingDate());
        credit.setClosingDate(req.closingDate());
        credit = creditRepo.save(credit);

        CreditCard savedCard = null;
        if (req.type() == CreditType.CREDIT_CARD && req.cardDetail() != null) {
            CreditCard existing = cardRepo.findByCreditId(id).orElse(null);
            if (existing != null) {
                updateCardDetail(existing, req.cardDetail());
                savedCard = cardRepo.save(existing);
            } else {
                savedCard = saveCardDetail(id, userId, req.cardDetail());
            }
        }

        CreditCardDetailResponse cardDto = savedCard != null ? CreditCardDetailResponse.from(savedCard)
                : cardRepo.findByCreditId(id).map(CreditCardDetailResponse::from).orElse(null);
        BigDecimal ea = calculateEa(credit.getInterestRateMv());
        CreditAlertLevel alert = analysisUseCase.resolveAlertLevel(credit, savedCard);
        return CreditResponse.from(credit, cardDto, ea, alert);
    }

    @Transactional(readOnly = true)
    public List<CreditResponse> listByUser(Long userId) {
        return creditRepo.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(c -> {
                    CreditCard card = cardRepo.findByCreditId(c.getId()).orElse(null);
                    CreditCardDetailResponse cardDto = card != null ? CreditCardDetailResponse.from(card) : null;
                    BigDecimal ea = calculateEa(c.getInterestRateMv());
                    CreditAlertLevel alert = analysisUseCase.resolveAlertLevel(c, card);
                    return CreditResponse.from(c, cardDto, ea, alert);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CreditResponse getById(Long userId, Long id) {
        Credit credit = getOwned(id, userId);
        CreditCard card = cardRepo.findByCreditId(id).orElse(null);
        CreditCardDetailResponse cardDto = card != null ? CreditCardDetailResponse.from(card) : null;
        BigDecimal ea = calculateEa(credit.getInterestRateMv());
        CreditAlertLevel alert = analysisUseCase.resolveAlertLevel(credit, card);
        return CreditResponse.from(credit, cardDto, ea, alert);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        Credit credit = getOwned(id, userId);
        credit.setDeletedAt(Instant.now());
        credit.setDeletedBy(userId);
        creditRepo.save(credit);
    }

    @Transactional
    public CreditPaymentResponse registerPayment(Long userId, Long creditId, CreditPaymentRequest req) {
        Credit credit = getOwned(creditId, userId);

        if (paymentRepo.findByCreditIdAndPeriodMonthAndPeriodYear(creditId, req.month(), req.year()).isPresent()) {
            throw new BusinessException("Ya existe un pago registrado para este período");
        }

        CreditPayment payment = CreditPayment.builder()
                .userId(userId)
                .creditId(creditId)
                .periodMonth(req.month())
                .periodYear(req.year())
                .amount(req.amount())
                .paymentDate(req.paymentDate())
                .notes(req.notes())
                .build();
        payment = paymentRepo.save(payment);

        // Reducir saldo del crédito
        BigDecimal newBalance = credit.getCurrentBalance().subtract(req.amount());
        credit.setCurrentBalance(newBalance.max(BigDecimal.ZERO));
        if (credit.getType() != CreditType.CREDIT_CARD) {
            credit.setPaidInstallments(credit.getPaidInstallments() + 1);
        }
        creditRepo.save(credit);

        return CreditPaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public List<CreditPaymentResponse> listPayments(Long userId, Long creditId) {
        getOwned(creditId, userId);
        return paymentRepo.findByCreditIdOrderByPeriodYearDescPeriodMonthDesc(creditId)
                .stream().map(CreditPaymentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CreditSummaryResponse getSummary(Long userId) {
        List<Credit> all = creditRepo.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        List<Credit> active = creditRepo.findByUserIdAndStatusAndDeletedAtIsNull(userId, CreditStatus.ACTIVE);

        BigDecimal totalDebt = creditRepo.sumActiveBalanceByUser(userId);
        BigDecimal monthlyInstallments = creditRepo.sumMonthlyInstallmentsByUser(userId);
        BigDecimal cardUsed = cardRepo.sumUsedBalanceByUser(userId);
        BigDecimal cardMinPayments = cardRepo.sumMinimumPaymentsByUser(userId);

        List<CreditCard> cards = cardRepo.findByUserId(userId);
        long lateInterestCount = cards.stream()
                .filter(cc -> cc.getLateInterest().compareTo(BigDecimal.ZERO) > 0).count();
        long maxCapacityCount = cards.stream()
                .filter(cc -> cc.getAvailablePurchases().compareTo(BigDecimal.ZERO) <= 0).count();

        return new CreditSummaryResponse(
                all.size(),
                active.size(),
                totalDebt,
                monthlyInstallments.add(cardMinPayments),
                cardUsed,
                cardMinPayments,
                (int) lateInterestCount,
                (int) maxCapacityCount
        );
    }

    private Credit getOwned(Long id, Long userId) {
        return creditRepo.findByIdAndUserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Crédito no encontrado"));
    }

    private CreditCard saveCardDetail(Long creditId, Long userId, CreditCardDetailRequest d) {
        return cardRepo.save(CreditCard.builder()
                .creditId(creditId)
                .userId(userId)
                .cardNumberLast4(d.cardNumberLast4())
                .brand(d.brand())
                .creditLimitPurchases(d.creditLimitPurchases())
                .creditLimitAdvances(d.creditLimitAdvances())
                .availablePurchases(d.availablePurchases())
                .availableAdvances(d.availableAdvances())
                .previousBalance(d.previousBalance())
                .minimumPayment(d.minimumPayment())
                .alternateMinimumPayment(d.alternateMinimumPayment())
                .lateInterest(d.lateInterest())
                .paymentDueDay(d.paymentDueDay())
                .build());
    }

    private void updateCardDetail(CreditCard cc, CreditCardDetailRequest d) {
        cc.setCardNumberLast4(d.cardNumberLast4());
        cc.setBrand(d.brand());
        cc.setCreditLimitPurchases(d.creditLimitPurchases());
        cc.setCreditLimitAdvances(d.creditLimitAdvances());
        cc.setAvailablePurchases(d.availablePurchases());
        cc.setAvailableAdvances(d.availableAdvances());
        cc.setPreviousBalance(d.previousBalance());
        cc.setMinimumPayment(d.minimumPayment());
        cc.setAlternateMinimumPayment(d.alternateMinimumPayment());
        cc.setLateInterest(d.lateInterest());
        cc.setPaymentDueDay(d.paymentDueDay());
    }

    // EA = (1 + MV/100)^12 - 1
    static BigDecimal calculateEa(BigDecimal mv) {
        if (mv == null || mv.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal base = BigDecimal.ONE.add(mv.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP));
        BigDecimal ea = base.pow(12, new MathContext(10, RoundingMode.HALF_UP)).subtract(BigDecimal.ONE);
        return ea.multiply(new BigDecimal("100")).setScale(4, RoundingMode.HALF_UP);
    }
}
