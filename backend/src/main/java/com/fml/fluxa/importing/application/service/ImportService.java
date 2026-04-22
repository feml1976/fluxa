package com.fml.fluxa.importing.application.service;

import com.fml.fluxa.expense.domain.model.VariableExpense;
import com.fml.fluxa.expense.infrastructure.persistence.ExpenseCategoryJpaRepository;
import com.fml.fluxa.expense.infrastructure.persistence.VariableExpenseJpaRepository;
import com.fml.fluxa.importing.application.dto.ImportExecuteResponse;
import com.fml.fluxa.importing.application.dto.ImportPreviewResponse;
import com.fml.fluxa.importing.application.dto.ImportRowResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportService {

    private static final DateTimeFormatter FMT_SLASH = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_ISO   = DateTimeFormatter.ISO_LOCAL_DATE;

    private final VariableExpenseJpaRepository expenseRepo;
    private final ExpenseCategoryJpaRepository categoryRepo;

    public ImportService(VariableExpenseJpaRepository expenseRepo,
                         ExpenseCategoryJpaRepository categoryRepo) {
        this.expenseRepo = expenseRepo;
        this.categoryRepo = categoryRepo;
    }

    public ImportPreviewResponse preview(Long userId, MultipartFile file) throws Exception {
        List<ImportRowResult> rows = parseFile(userId, file);
        long valid   = rows.stream().filter(ImportRowResult::valid).count();
        long invalid = rows.size() - valid;
        return new ImportPreviewResponse(rows.size(), (int) valid, (int) invalid, rows);
    }

    @Transactional
    public ImportExecuteResponse execute(Long userId, MultipartFile file) throws Exception {
        List<ImportRowResult> rows = parseFile(userId, file);
        int imported = 0;
        int skipped  = 0;

        for (ImportRowResult row : rows) {
            if (!row.valid()) { skipped++; continue; }
            try {
                BigDecimal amount = new BigDecimal(row.resolvedAmount());
                LocalDate date    = parseDate(row.resolvedDate());
                Long categoryId   = resolveDefaultCategory(userId);

                expenseRepo.save(VariableExpense.builder()
                        .userId(userId)
                        .categoryId(categoryId)
                        .amount(amount)
                        .expenseDate(date)
                        .description(row.resolvedDescription())
                        .build());
                imported++;
            } catch (Exception e) {
                skipped++;
            }
        }
        return new ImportExecuteResponse(imported, skipped,
                "Importación completada: " + imported + " filas guardadas, " + skipped + " omitidas.");
    }

    // ── Parsing ──────────────────────────────────────────────────

    private List<ImportRowResult> parseFile(Long userId, MultipartFile file) throws Exception {
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            return parseExcel(userId, file);
        }
        return parseCsv(userId, file);
    }

    private List<ImportRowResult> parseCsv(Long userId, MultipartFile file) throws Exception {
        List<ImportRowResult> results = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // omitir encabezado
            if (header == null) return results;

            String line;
            int rowNum = 2;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) { rowNum++; continue; }
                String[] cols = line.split(",", -1);
                results.add(validateRow(rowNum++, line, cols));
            }
        }
        return results;
    }

    private List<ImportRowResult> parseExcel(Long userId, MultipartFile file) throws Exception {
        List<ImportRowResult> results = new ArrayList<>();
        try (Workbook wb = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            boolean first = true;
            for (Row row : sheet) {
                if (first) { first = false; continue; } // encabezado
                if (row == null) continue;

                String[] cols = new String[4];
                for (int i = 0; i < 4; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cols[i] = cellToString(cell);
                }
                String raw = String.join(",", cols);
                results.add(validateRow(row.getRowNum() + 1, raw, cols));
            }
        }
        return results;
    }

    private String cellToString(Cell cell) {
        return switch (cell.getCellType()) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield FMT_SLASH.format(cell.getLocalDateTimeCellValue().toLocalDate());
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case STRING  -> cell.getStringCellValue().trim();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    // ── Validación de fila ────────────────────────────────────────

    private ImportRowResult validateRow(int rowNum, String raw, String[] cols) {
        List<String> errors = new ArrayList<>();

        String tipo        = cols.length > 0 ? cols[0].trim() : "";
        String descripcion = cols.length > 1 ? cols[1].trim() : "";
        String montoStr    = cols.length > 2 ? cols[2].trim() : "";
        String fechaStr    = cols.length > 3 ? cols[3].trim() : "";

        if (!tipo.equalsIgnoreCase("gasto")) {
            errors.add("Tipo inválido '" + tipo + "' — solo se acepta 'gasto'");
        }
        if (descripcion.isBlank()) {
            errors.add("Descripción vacía");
        }

        BigDecimal monto = null;
        try {
            monto = new BigDecimal(montoStr.replace(".", "").replace(",", "."));
            if (monto.compareTo(BigDecimal.ZERO) <= 0) errors.add("Monto debe ser mayor a 0");
        } catch (NumberFormatException e) {
            errors.add("Monto inválido: '" + montoStr + "'");
        }

        LocalDate fecha = null;
        try {
            fecha = parseDate(fechaStr);
        } catch (Exception e) {
            errors.add("Fecha inválida: '" + fechaStr + "' — use DD/MM/YYYY o YYYY-MM-DD");
        }

        boolean valid = errors.isEmpty();
        return new ImportRowResult(
                rowNum, raw, valid, errors,
                valid ? "gasto" : null,
                valid ? descripcion : null,
                valid ? (monto != null ? monto.toPlainString() : null) : null,
                valid ? (fecha != null ? fecha.toString() : null) : null
        );
    }

    // ── Utilidades ────────────────────────────────────────────────

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) throw new DateTimeParseException("vacío", s != null ? s : "", 0);
        try { return LocalDate.parse(s, FMT_SLASH); } catch (DateTimeParseException ignored) {}
        return LocalDate.parse(s, FMT_ISO);
    }

    private Long resolveDefaultCategory(Long userId) {
        return categoryRepo.findByUserIdAndDeletedAtIsNullOrderByNameAsc(userId)
                .stream().findFirst().map(c -> c.getId()).orElse(null);
    }
}
