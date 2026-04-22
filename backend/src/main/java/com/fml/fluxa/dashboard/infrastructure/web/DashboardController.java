package com.fml.fluxa.dashboard.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.dashboard.application.dto.DashboardSummaryResponse;
import com.fml.fluxa.dashboard.application.usecase.GetDashboardSummaryUseCase;
import com.fml.fluxa.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final GetDashboardSummaryUseCase useCase;

    public DashboardController(GetDashboardSummaryUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal User user,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getSummary(user.getId(), month, year)));
    }
}
