package com.fml.fluxa.commitment.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.commitment.application.dto.*;
import com.fml.fluxa.commitment.application.usecase.*;
import com.fml.fluxa.expense.domain.model.ExpenseCategoryType;
import com.fml.fluxa.shared.infrastructure.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Compromisos Fijos", description = "Arriendo, servicios públicos, seguros y otros pagos recurrentes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/commitments")
public class CommitmentController {

    private final ExpenseCategoryUseCase categoryUseCase;
    private final FixedCommitmentUseCase commitmentUseCase;
    private final MonthlyCommitmentUseCase monthlyUseCase;

    public CommitmentController(ExpenseCategoryUseCase categoryUseCase,
                                 FixedCommitmentUseCase commitmentUseCase,
                                 MonthlyCommitmentUseCase monthlyUseCase) {
        this.categoryUseCase   = categoryUseCase;
        this.commitmentUseCase = commitmentUseCase;
        this.monthlyUseCase    = monthlyUseCase;
    }

    // ── Categorías de gasto ─────────────────────────────────────

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<ExpenseCategoryResponse>> createCategory(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ExpenseCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(categoryUseCase.create(user.getId(), req)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<ExpenseCategoryResponse>>> listCategories(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) ExpenseCategoryType type) {
        return ResponseEntity.ok(ApiResponse.ok(categoryUseCase.listByUser(user.getId(), type)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<ExpenseCategoryResponse>> updateCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(categoryUseCase.update(user.getId(), id, req)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        categoryUseCase.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Categoría eliminada", null));
    }

    // ── Compromisos fijos ───────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<FixedCommitmentResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FixedCommitmentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(commitmentUseCase.create(user.getId(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FixedCommitmentResponse>>> list(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(commitmentUseCase.listByUser(user.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FixedCommitmentResponse>> getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(commitmentUseCase.getById(user.getId(), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FixedCommitmentResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody FixedCommitmentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(commitmentUseCase.update(user.getId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        commitmentUseCase.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Compromiso eliminado", null));
    }

    // ── Registros mensuales ─────────────────────────────────────

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyCommitmentSummary>> getMonthly(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyUseCase.getMonthly(user.getId(), month, year)));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<CommitmentRecordResponse>> registerPayment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam int month,
            @RequestParam int year,
            @Valid @RequestBody RegisterPaymentRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                monthlyUseCase.registerPayment(user.getId(), id, month, year, req)));
    }
}
