package com.fml.fluxa.dashboard.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.dashboard.application.dto.DashboardSummaryResponse;
import com.fml.fluxa.dashboard.application.dto.DebtStrategyResponse;
import com.fml.fluxa.dashboard.application.dto.ProjectionsResponse;
import com.fml.fluxa.dashboard.application.usecase.GetDashboardSummaryUseCase;
import com.fml.fluxa.dashboard.application.usecase.GetDebtStrategyUseCase;
import com.fml.fluxa.dashboard.application.usecase.GetProjectionsUseCase;
import com.fml.fluxa.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final GetDashboardSummaryUseCase summaryUseCase;
    private final GetProjectionsUseCase projectionsUseCase;
    private final GetDebtStrategyUseCase debtStrategyUseCase;

    public DashboardController(GetDashboardSummaryUseCase summaryUseCase,
                               GetProjectionsUseCase projectionsUseCase,
                               GetDebtStrategyUseCase debtStrategyUseCase) {
        this.summaryUseCase = summaryUseCase;
        this.projectionsUseCase = projectionsUseCase;
        this.debtStrategyUseCase = debtStrategyUseCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(summaryUseCase.getSummary(user.getId(), month, year)));
    }

    @GetMapping("/projections")
    public ResponseEntity<ApiResponse<ProjectionsResponse>> getProjections(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "6") int months) {
        int clamped = Math.min(Math.max(months, 1), 12);
        return ResponseEntity.ok(ApiResponse.ok(projectionsUseCase.getProjections(user.getId(), clamped)));
    }

    @GetMapping("/debt-strategy")
    public ResponseEntity<ApiResponse<List<DebtStrategyResponse>>> getDebtStrategy(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.ok(debtStrategyUseCase.getStrategies(user.getId())));
    }
}
