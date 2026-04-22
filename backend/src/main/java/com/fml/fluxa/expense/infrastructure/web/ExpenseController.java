package com.fml.fluxa.expense.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.expense.application.dto.*;
import com.fml.fluxa.expense.application.usecase.BudgetPlanUseCase;
import com.fml.fluxa.expense.application.usecase.MonthlyExpenseUseCase;
import com.fml.fluxa.expense.application.usecase.VariableExpenseUseCase;
import com.fml.fluxa.shared.infrastructure.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {

    private final VariableExpenseUseCase expenseUseCase;
    private final BudgetPlanUseCase budgetUseCase;
    private final MonthlyExpenseUseCase monthlyUseCase;

    public ExpenseController(VariableExpenseUseCase expenseUseCase,
                             BudgetPlanUseCase budgetUseCase,
                             MonthlyExpenseUseCase monthlyUseCase) {
        this.expenseUseCase = expenseUseCase;
        this.budgetUseCase = budgetUseCase;
        this.monthlyUseCase = monthlyUseCase;
    }

    // ── Gastos Variables ────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<VariableExpenseResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VariableExpenseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(expenseUseCase.create(user.getId(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VariableExpenseResponse>>> list(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(expenseUseCase.listByPeriod(user.getId(), month, year)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VariableExpenseResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody VariableExpenseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(expenseUseCase.update(user.getId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        expenseUseCase.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Gasto eliminado", null));
    }

    // ── Presupuestos ────────────────────────────────────────────

    @PostMapping("/budgets")
    public ResponseEntity<ApiResponse<BudgetPlanResponse>> saveOrUpdateBudget(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BudgetPlanRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(budgetUseCase.saveOrUpdate(user.getId(), req)));
    }

    @GetMapping("/budgets")
    public ResponseEntity<ApiResponse<List<BudgetPlanResponse>>> listBudgets(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(budgetUseCase.listByPeriod(user.getId(), month, year)));
    }

    @DeleteMapping("/budgets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        budgetUseCase.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Presupuesto eliminado", null));
    }

    // ── Resumen Mensual ─────────────────────────────────────────

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyExpenseSummary>> getMonthly(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyUseCase.getSummary(user.getId(), month, year)));
    }
}
