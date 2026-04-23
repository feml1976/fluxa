package com.fml.fluxa.income.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.income.application.dto.*;
import com.fml.fluxa.income.application.usecase.*;
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

@Tag(name = "Ingresos", description = "Categorías de ingreso, fuentes y registros mensuales")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/income")
public class IncomeController {

    private final IncomeCategoryUseCase categoryUseCase;
    private final IncomeSourceUseCase sourceUseCase;
    private final MonthlyIncomeUseCase monthlyUseCase;

    public IncomeController(IncomeCategoryUseCase categoryUseCase,
                             IncomeSourceUseCase sourceUseCase,
                             MonthlyIncomeUseCase monthlyUseCase) {
        this.categoryUseCase = categoryUseCase;
        this.sourceUseCase   = sourceUseCase;
        this.monthlyUseCase  = monthlyUseCase;
    }

    // ── Categorías ──────────────────────────────────────────────

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<IncomeCategoryResponse>> createCategory(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody IncomeCategoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(categoryUseCase.create(user.getId(), req)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<IncomeCategoryResponse>>> listCategories(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(categoryUseCase.listByUser(user.getId())));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<IncomeCategoryResponse>> updateCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody IncomeCategoryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(categoryUseCase.update(user.getId(), id, req)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        categoryUseCase.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Categoría eliminada", null));
    }

    // ── Fuentes de ingreso ──────────────────────────────────────

    @PostMapping("/sources")
    public ResponseEntity<ApiResponse<IncomeSourceResponse>> createSource(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody IncomeSourceRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(sourceUseCase.create(user.getId(), req)));
    }

    @GetMapping("/sources")
    public ResponseEntity<ApiResponse<List<IncomeSourceResponse>>> listSources(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(sourceUseCase.listByUser(user.getId())));
    }

    @GetMapping("/sources/{id}")
    public ResponseEntity<ApiResponse<IncomeSourceResponse>> getSource(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sourceUseCase.getById(user.getId(), id)));
    }

    @PutMapping("/sources/{id}")
    public ResponseEntity<ApiResponse<IncomeSourceResponse>> updateSource(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody IncomeSourceRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(sourceUseCase.update(user.getId(), id, req)));
    }

    @DeleteMapping("/sources/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSource(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        sourceUseCase.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Fuente de ingreso eliminada", null));
    }

    // ── Registros mensuales ─────────────────────────────────────

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<MonthlyIncomeSummary>> getMonthly(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyUseCase.getMonthly(user.getId(), month, year)));
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<ApiResponse<IncomeRecordResponse>> updateRecord(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncomeRecordRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(monthlyUseCase.updateRecord(user.getId(), id, req)));
    }
}
