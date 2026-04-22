package com.fml.fluxa.dashboard.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryResponse(
        int month,
        int year,

        // Ingresos
        BigDecimal totalIncome,
        BigDecimal totalIncomeExpected,

        // Compromisos fijos
        BigDecimal totalCommitments,
        BigDecimal totalCommitmentsPaid,
        int pendingCommitmentsCount,
        int overdueCommitmentsCount,

        // Gastos variables
        BigDecimal totalExpenses,
        BigDecimal totalExpensesPlanned,

        // Flujo neto
        BigDecimal netFlow,

        // Indicador de salud financiera
        BigDecimal commitmentRatio,
        HealthStatus healthStatus,

        // Próximos vencimientos (7 días)
        List<UpcomingPaymentDto> upcomingPayments,

        // Top 5 categorías de gasto
        List<TopExpenseDto> topExpenses
) {
    public enum HealthStatus { GREEN, YELLOW, RED }
}
