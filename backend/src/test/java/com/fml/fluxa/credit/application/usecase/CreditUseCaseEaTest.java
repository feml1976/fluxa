package com.fml.fluxa.credit.application.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para el método estático calculateEa() que convierte
 * tasa MV (Mensual Vencida) a tasa EA (Efectiva Anual).
 * Fórmula: EA = (1 + MV/100)^12 - 1
 */
@DisplayName("CreditUseCase.calculateEa()")
class CreditUseCaseEaTest {

    @Test
    @DisplayName("Retorna 0 cuando la tasa MV es cero")
    void calculateEa_returnsZero_whenMvIsZero() {
        BigDecimal ea = CreditUseCase.calculateEa(BigDecimal.ZERO);
        assertThat(ea).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Retorna 0 cuando la tasa MV es nula")
    void calculateEa_returnsZero_whenMvIsNull() {
        BigDecimal ea = CreditUseCase.calculateEa(null);
        assertThat(ea).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Calcula correctamente EA para MV = 2% → EA ≈ 26.8242%")
    void calculateEa_correct_forMv2Pct() {
        // (1 + 0.02)^12 - 1 = 1.02^12 - 1 ≈ 0.268242 → 26.8242%
        BigDecimal ea = CreditUseCase.calculateEa(new BigDecimal("2.0"));
        assertThat(ea).isBetween(new BigDecimal("26.80"), new BigDecimal("26.83"));
    }

    @Test
    @DisplayName("Calcula correctamente EA para MV = 1% → EA ≈ 12.6825%")
    void calculateEa_correct_forMv1Pct() {
        // (1 + 0.01)^12 - 1 ≈ 0.126825 → 12.6825%
        BigDecimal ea = CreditUseCase.calculateEa(new BigDecimal("1.0"));
        assertThat(ea).isBetween(new BigDecimal("12.67"), new BigDecimal("12.70"));
    }

    @Test
    @DisplayName("EA siempre es mayor que MV*12 (efecto del interés compuesto)")
    void calculateEa_isAlwaysGreaterThanSimpleAnnualRate() {
        BigDecimal mv = new BigDecimal("1.5");
        BigDecimal simpleAnnual = mv.multiply(new BigDecimal("12")); // 18%
        BigDecimal ea = CreditUseCase.calculateEa(mv);
        assertThat(ea).isGreaterThan(simpleAnnual);
    }
}
