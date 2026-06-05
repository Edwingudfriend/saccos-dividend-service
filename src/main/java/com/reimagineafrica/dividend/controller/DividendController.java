package com.reimagineafrica.dividend.controller;

import com.reimagineafrica.dividend.dto.*;
import com.reimagineafrica.dividend.service.DividendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dividends")
@RequiredArgsConstructor
@Tag(name = "Dividend Engine", description = "Year-end dividend calculation and disbursement")
public class DividendController {

    private final DividendService dividendService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BOARD_MEMBER')")
    @Operation(summary = "Create a new dividend cycle for a fiscal year")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DividendCycleResponse> createCycle(
            @RequestParam Integer fiscalYear,
            @RequestParam BigDecimal totalProfit,
            @RequestParam BigDecimal distributableProfit,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(dividendService.createCycle(fiscalYear, totalProfit, distributableProfit, notes));
    }

    @GetMapping("/{fiscalYear}/preview")
    @PreAuthorize("hasAnyRole('ADMIN','BOARD_MEMBER','MANAGER')")
    @Operation(summary = "Preview dividend simulation before board approval")
    public ResponseEntity<PreviewResponse> previewDividends(@PathVariable Integer fiscalYear) {
        return ResponseEntity.ok(dividendService.previewDividends(fiscalYear));
    }

    @PostMapping("/{fiscalYear}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','BOARD_MEMBER')")
    @Operation(summary = "Board approves dividend distribution")
    public ResponseEntity<DividendCycleResponse> approveCycle(
            @PathVariable Integer fiscalYear,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(dividendService.approveCycle(fiscalYear, approvedBy));
    }

    @PostMapping("/{fiscalYear}/disburse")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Disburse dividends to all member savings accounts")
    public ResponseEntity<DisbursementSummary> disburse(@PathVariable Integer fiscalYear) {
        return ResponseEntity.ok(dividendService.disburseDividends(fiscalYear));
    }
}
