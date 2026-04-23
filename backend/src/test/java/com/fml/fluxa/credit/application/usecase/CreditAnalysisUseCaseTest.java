package com.fml.fluxa.credit.application.usecase;

import com.fml.fluxa.credit.application.dto.CreditAlertLevel;
import com.fml.fluxa.credit.application.dto.CreditAnalysisResponse;
import com.fml.fluxa.credit.domain.model.CardBrand;
import com.fml.fluxa.credit.domain.model.Credit;
import com.fml.fluxa.credit.domain.model.CreditCard;
import com.fml.fluxa.credit.domain.model.CreditStatus;
import com.fml.fluxa.credit.domain.model.CreditType;
import com.fml.fluxa.credit.infrastructure.persistence.CreditCardJpaRepository;
import com.fml.fluxa.credit.infrastructure.persistence.CreditJpaRepository;
import com.fml.fluxa.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditAnalysisUseCase")
class CreditAnalysisUseCaseTest {

    @Mock CreditJpaRepository creditRepo;
    @Mock CreditCardJpaRepository cardRepo;
    @InjectMocks CreditAnalysisUseCase useCase;

    // ── Constructores de prueba ────────────────────────────────────

    private Credit buildLoan(BigDecimal balance, BigDecimal installment, BigDecimal mv) {
        return Credit.builder()
                .id(1L)
                .userId(10L)
                .type(CreditType.PERSONAL)
                .status(CreditStatus.ACTIVE)
                .name("Crédito Personal")
                .interestRateMv(mv)
                .currentBalance(balance)
                .monthlyInstallment(installment)
                .totalInstallments(36)
                .paidInstallments(0)
                .openingDate(LocalDate.now().minusMonths(6))
                .build();
    }

    private Credit buildCreditCardCredit(BigDecimal mv) {
        return Credit.builder()
                .id(2L)
                .userId(10L)
                .type(CreditType.CREDIT_CARD)
                .status(CreditStatus.ACTIVE)
                .name("Tarjeta Visa")
                .interestRateMv(mv)
                .currentBalance(BigDecimal.ZERO)
                .paidInstallments(0)
                .openingDate(LocalDate.now().minusYears(1))
                .build();
    }

    private CreditCard buildCard(BigDecimal limit, BigDecimal available,
                                  BigDecimal minPayment, BigDecimal alternateMin,
                                  BigDecimal lateInterest) {
        return CreditCard.builder()
                .id(1L)
                .creditId(2L)
                .userId(10L)
                .cardNumberLast4("1234")
                .brand(CardBrand.VISA)
                .creditLimitPurchases(limit)
                .creditLimitAdvances(new BigDecimal("500000"))
                .availablePurchases(available)
                .availableAdvances(new BigDecimal("400000"))
                .previousBalance(BigDecimal.ZERO)
                .minimumPayment(minPayment)
                .alternateMinimumPayment(alternateMin)
                .lateInterest(lateInterest)
                .paymentDueDay(15)
                .build();
    }

    // ── Tarjeta de Crédito ─────────────────────────────────────────

    @Nested
    @DisplayName("Análisis tarjeta de crédito")
    class TarjetaTests {

        @Test
        @DisplayName("GREEN cuando utilización < 80% y sin mora")
        void analyze_creditCard_greenAlert_whenUtilizacionBelow80Pct() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            // Límite 1M | Disponible 500K → utilización 50%
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("500000"),
                    new BigDecimal("50000"), BigDecimal.ZERO, BigDecimal.ZERO);

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);

            assertThat(result.alertLevel()).isEqualTo(CreditAlertLevel.GREEN);
            assertThat(result.utilizationPct()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(result.alerts()).isEmpty();
        }

        @Test
        @DisplayName("YELLOW cuando utilización >= 80%")
        void analyze_creditCard_yellowAlert_whenUtilizacionAt85Pct() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            // Límite 1M | Disponible 150K → utilización 85%
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("150000"),
                    new BigDecimal("50000"), BigDecimal.ZERO, BigDecimal.ZERO);

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);

            assertThat(result.alertLevel()).isEqualTo(CreditAlertLevel.YELLOW);
            assertThat(result.alerts()).anyMatch(a -> a.contains("Utilización alta"));
            assertThat(result.utilizationPct()).isEqualByComparingTo(new BigDecimal("85.00"));
        }

        @Test
        @DisplayName("RED cuando existen intereses de mora")
        void analyze_creditCard_redAlert_whenLateInterestExists() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("500000"),
                    new BigDecimal("50000"), BigDecimal.ZERO, new BigDecimal("25000"));

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);

            assertThat(result.alertLevel()).isEqualTo(CreditAlertLevel.RED);
            assertThat(result.alerts()).anyMatch(a -> a.contains("ALERTA CRÍTICA"));
        }

        @Test
        @DisplayName("RED cuando cupo disponible es cero")
        void analyze_creditCard_redAlert_whenCupoAgotado() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            // availablePurchases = 0 → cupo agotado
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), BigDecimal.ZERO,
                    new BigDecimal("50000"), BigDecimal.ZERO, BigDecimal.ZERO);

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);

            assertThat(result.alertLevel()).isEqualTo(CreditAlertLevel.RED);
            assertThat(result.alerts()).anyMatch(a -> a.contains("Cupo disponible agotado"));
        }

        @Test
        @DisplayName("Advertencia de Pago Mínimo Alterno cuando alternateMinimumPayment > 0")
        void analyze_creditCard_hasAlternateMinimumWarning() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("500000"),
                    new BigDecimal("50000"), new BigDecimal("20000"), BigDecimal.ZERO);

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);

            assertThat(result.alternateMinimumWarning()).isTrue();
            assertThat(result.alerts()).anyMatch(a -> a.contains("ADVERTENCIA"));
        }

        @Test
        @DisplayName("Proyección con pago mínimo retorna meses > 0 e intereses > 0")
        void analyze_creditCard_calculatesMonthsAndInterestWithMinimumPayment() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            // Usado = 1M - 500K = 500K | Pago mínimo 50K | MV 1.5%
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("500000"),
                    new BigDecimal("50000"), BigDecimal.ZERO, BigDecimal.ZERO);

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);

            assertThat(result.monthsToPayMinimum()).isNotNull().isPositive();
            assertThat(result.totalInterestWithMinimum()).isNotNull()
                    .isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("monthsToPayMinimum es null cuando la cuota mínima no cubre los intereses")
        void analyze_creditCard_returnsNullMonths_whenMinimumPaymentCannotCoverInterest() {
            Credit credit = buildCreditCardCredit(new BigDecimal("2.0"));
            // Usado = 500K | MV 2% → interés = 10K | Pago mínimo 5K (< 10K)
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("500000"),
                    new BigDecimal("5000"), BigDecimal.ZERO, BigDecimal.ZERO);

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);

            assertThat(result.monthsToPayMinimum()).isNull();
            assertThat(result.totalInterestWithMinimum()).isNull();
        }

        @Test
        @DisplayName("Utilización = 0 cuando el límite de compras es cero (sin división por cero)")
        void analyze_creditCard_zeroUtilization_whenCreditLimitIsZero() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            CreditCard card = buildCard(
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.of(card));

            assertThatCode(() -> useCase.analyze(10L, 2L)).doesNotThrowAnyException();

            CreditAnalysisResponse result = useCase.analyze(10L, 2L);
            assertThat(result.utilizationPct()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    // ── Créditos (personal/hipoteca/vehículo) ─────────────────────

    @Nested
    @DisplayName("Análisis crédito tradicional (préstamos)")
    class PrestamoTests {

        @Test
        @DisplayName("GREEN con datos válidos — retorna tabla de amortización y cuotas restantes")
        void analyze_loan_greenAlert_withValidData() {
            // balance = 10M | installment = 500K | MV = 1.5%
            Credit credit = buildLoan(
                    new BigDecimal("10000000"),
                    new BigDecimal("500000"),
                    new BigDecimal("1.5"));

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(1L, 10L)).thenReturn(Optional.of(credit));

            CreditAnalysisResponse result = useCase.analyze(10L, 1L);

            assertThat(result.alertLevel()).isEqualTo(CreditAlertLevel.GREEN);
            assertThat(result.remainingInstallments()).isNotNull().isPositive();
            assertThat(result.projectedPayoffDate()).isNotNull().isAfter(LocalDate.now());
            assertThat(result.amortizationTable()).isNotEmpty();
            assertThat(result.totalRemainingInterest()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("YELLOW cuando la tasa MV supera el 2% mensual")
        void analyze_loan_yellowAlert_whenInterestRateMvAbove2Pct() {
            Credit credit = buildLoan(
                    new BigDecimal("10000000"),
                    new BigDecimal("500000"),
                    new BigDecimal("3.0")); // tasa > 2%

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(1L, 10L)).thenReturn(Optional.of(credit));

            CreditAnalysisResponse result = useCase.analyze(10L, 1L);

            assertThat(result.alertLevel()).isEqualTo(CreditAlertLevel.YELLOW);
            assertThat(result.alerts()).anyMatch(a -> a.contains("Tasa MV"));
        }

        @Test
        @DisplayName("Alerta cuando quedan más de 120 cuotas (más de 10 años)")
        void analyze_loan_hasAlert_whenRemainingInstallmentsExceed120() {
            // balance = 50M | installment = 400K | MV = 0.5%
            // → n ≈ 197 cuotas > 120
            Credit credit = buildLoan(
                    new BigDecimal("50000000"),
                    new BigDecimal("400000"),
                    new BigDecimal("0.5"));

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(1L, 10L)).thenReturn(Optional.of(credit));

            CreditAnalysisResponse result = useCase.analyze(10L, 1L);

            assertThat(result.remainingInstallments()).isGreaterThan(120);
            assertThat(result.alerts()).anyMatch(a -> a.contains("10 años"));
        }

        @Test
        @DisplayName("Respuesta vacía cuando la cuota mensual es nula o cero")
        void analyze_loan_returnsEmptyResponse_whenInstallmentIsNullOrZero() {
            Credit credit = buildLoan(
                    new BigDecimal("10000000"),
                    null,                // sin cuota configurada
                    new BigDecimal("1.5"));

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(1L, 10L)).thenReturn(Optional.of(credit));

            CreditAnalysisResponse result = useCase.analyze(10L, 1L);

            assertThat(result.alertLevel()).isEqualTo(CreditAlertLevel.GREEN);
            assertThat(result.remainingInstallments()).isNull();
            assertThat(result.amortizationTable()).isEmpty();
        }

        @Test
        @DisplayName("Primera fila de amortización tiene valores matemáticamente correctos")
        void analyze_loan_firstAmortizationRow_hasCorrectValues() {
            // balance = 10M | installment = 500K | MV = 1.5%
            // Fila 1: interés = 10M * 0.015 = 150K; capital = 500K - 150K = 350K; saldo = 9.65M
            Credit credit = buildLoan(
                    new BigDecimal("10000000"),
                    new BigDecimal("500000"),
                    new BigDecimal("1.5"));

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(1L, 10L)).thenReturn(Optional.of(credit));

            CreditAnalysisResponse result = useCase.analyze(10L, 1L);

            var fila1 = result.amortizationTable().get(0);
            assertThat(fila1.installmentNumber()).isEqualTo(1);
            assertThat(fila1.interestPortion()).isEqualByComparingTo(new BigDecimal("150000.00"));
            assertThat(fila1.capitalPortion()).isEqualByComparingTo(new BigDecimal("350000.00"));
            assertThat(fila1.remainingBalance()).isEqualByComparingTo(new BigDecimal("9650000.00"));
        }

        @Test
        @DisplayName("Tabla limitada a 360 filas aunque queden más cuotas")
        void analyze_loan_amortizationTable_isCappedAt360Rows() {
            // Configurar un crédito de muy largo plazo
            Credit credit = buildLoan(
                    new BigDecimal("500000000"),  // 500M
                    new BigDecimal("300000"),     // cuota pequeña
                    new BigDecimal("0.1"));       // MV muy baja

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(1L, 10L)).thenReturn(Optional.of(credit));

            CreditAnalysisResponse result = useCase.analyze(10L, 1L);

            assertThat(result.amortizationTable()).hasSizeLessThanOrEqualTo(360);
        }
    }

    // ── resolveAlertLevel ──────────────────────────────────────────

    @Nested
    @DisplayName("resolveAlertLevel")
    class ResolveAlertLevelTests {

        @Test
        @DisplayName("GREEN para crédito no-tarjeta con tasa <= 2%")
        void resolveAlertLevel_green_forNonCardWithLowRate() {
            Credit credit = buildLoan(new BigDecimal("1000000"), new BigDecimal("100000"),
                    new BigDecimal("1.5"));

            CreditAlertLevel level = useCase.resolveAlertLevel(credit, null);

            assertThat(level).isEqualTo(CreditAlertLevel.GREEN);
        }

        @Test
        @DisplayName("YELLOW para crédito no-tarjeta con tasa > 2%")
        void resolveAlertLevel_yellow_forNonCardWithHighRate() {
            Credit credit = buildLoan(new BigDecimal("1000000"), new BigDecimal("100000"),
                    new BigDecimal("3.0"));

            CreditAlertLevel level = useCase.resolveAlertLevel(credit, null);

            assertThat(level).isEqualTo(CreditAlertLevel.YELLOW);
        }

        @Test
        @DisplayName("RED para tarjeta con intereses de mora")
        void resolveAlertLevel_red_forCardWithLateInterest() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("500000"),
                    new BigDecimal("50000"), BigDecimal.ZERO, new BigDecimal("15000"));

            CreditAlertLevel level = useCase.resolveAlertLevel(credit, card);

            assertThat(level).isEqualTo(CreditAlertLevel.RED);
        }

        @Test
        @DisplayName("RED para tarjeta con cupo de compras agotado")
        void resolveAlertLevel_red_forCardWithZeroAvailable() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), BigDecimal.ZERO,
                    new BigDecimal("50000"), BigDecimal.ZERO, BigDecimal.ZERO);

            CreditAlertLevel level = useCase.resolveAlertLevel(credit, card);

            assertThat(level).isEqualTo(CreditAlertLevel.RED);
        }

        @Test
        @DisplayName("YELLOW para tarjeta con utilización >= 80%")
        void resolveAlertLevel_yellow_forCardWithHighUtilization() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            // Disponible 100K de 1M → 90% usado
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("100000"),
                    new BigDecimal("50000"), BigDecimal.ZERO, BigDecimal.ZERO);

            CreditAlertLevel level = useCase.resolveAlertLevel(credit, card);

            assertThat(level).isEqualTo(CreditAlertLevel.YELLOW);
        }

        @Test
        @DisplayName("GREEN para tarjeta con utilización < 80% y sin mora")
        void resolveAlertLevel_green_forHealthyCard() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));
            // Disponible 600K de 1M → 40% usado
            CreditCard card = buildCard(
                    new BigDecimal("1000000"), new BigDecimal("600000"),
                    new BigDecimal("50000"), BigDecimal.ZERO, BigDecimal.ZERO);

            CreditAlertLevel level = useCase.resolveAlertLevel(credit, card);

            assertThat(level).isEqualTo(CreditAlertLevel.GREEN);
        }
    }

    // ── ResourceNotFoundException ──────────────────────────────────

    @Nested
    @DisplayName("Manejo de errores")
    class ErrorTests {

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando el crédito no existe o no pertenece al usuario")
        void analyze_throwsNotFound_whenCreditNotFound() {
            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(99L, 10L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.analyze(10L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Crédito");
        }

        @Test
        @DisplayName("Lanza ResourceNotFoundException cuando no existen datos de tarjeta para tipo CREDIT_CARD")
        void analyze_throwsNotFound_whenCardDataNotFound() {
            Credit credit = buildCreditCardCredit(new BigDecimal("1.5"));

            when(creditRepo.findByIdAndUserIdAndDeletedAtIsNull(2L, 10L)).thenReturn(Optional.of(credit));
            when(cardRepo.findByCreditId(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.analyze(10L, 2L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("tarjeta");
        }
    }
}
