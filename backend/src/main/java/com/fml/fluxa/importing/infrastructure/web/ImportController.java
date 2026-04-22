package com.fml.fluxa.importing.infrastructure.web;

import com.fml.fluxa.auth.domain.model.User;
import com.fml.fluxa.importing.application.dto.ImportExecuteResponse;
import com.fml.fluxa.importing.application.dto.ImportPreviewResponse;
import com.fml.fluxa.importing.application.service.ImportService;
import com.fml.fluxa.shared.infrastructure.web.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<ImportPreviewResponse>> preview(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws Exception {
        ImportPreviewResponse response = importService.preview(user.getId(), file);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ImportExecuteResponse>> execute(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws Exception {
        ImportExecuteResponse response = importService.execute(user.getId(), file);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
