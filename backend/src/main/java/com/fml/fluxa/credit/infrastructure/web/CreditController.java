package com.fml.fluxa.credit.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.credit.application.dto.*;
import com.fml.fluxa.credit.application.usecase.CreditAnalysisUseCase;
import com.fml.fluxa.credit.application.usecase.CreditUseCase;
import com.fml.fluxa.shared.infrastructure.web.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/credits")
public class CreditController {

    private final CreditUseCase creditUseCase;
    private final CreditAnalysisUseCase analysisUseCase;

    public CreditController(CreditUseCase creditUseCase, CreditAnalysisUseCase analysisUseCase) {
        this.creditUseCase = creditUseCase;
        this.analysisUseCase = analysisUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreditResponse>> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreditRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(creditUseCase.create(user.getId(), req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreditResponse>>> list(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(creditUseCase.listByUser(user.getId())));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CreditSummaryResponse>> getSummary(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(creditUseCase.getSummary(user.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CreditResponse>> getById(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(creditUseCase.getById(user.getId(), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CreditResponse>> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreditRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(creditUseCase.update(user.getId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        creditUseCase.delete(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Crédito eliminado", null));
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<ApiResponse<CreditAnalysisResponse>> analyze(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(analysisUseCase.analyze(user.getId(), id)));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<CreditPaymentResponse>> registerPayment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody CreditPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(creditUseCase.registerPayment(user.getId(), id, req)));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<ApiResponse<List<CreditPaymentResponse>>> listPayments(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(creditUseCase.listPayments(user.getId(), id)));
    }
}
