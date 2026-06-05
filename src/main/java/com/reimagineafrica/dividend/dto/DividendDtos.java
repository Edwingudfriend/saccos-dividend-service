package com.reimagineafrica.dividend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ── Request DTOs ──────────────────────────────────────────────────────────────

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class CreateCycleRequest {
    private Integer fiscalYear;
    private BigDecimal totalProfit;
    private BigDecimal distributableProfit; // board decides what % of profit to distribute
    private String notes;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
class ApproveCycleRequest {
    private String approvedBy;
    private String notes;
}

// ── Response DTOs ─────────────────────────────────────────────────────────────

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DividendCycleResponse {
    private Long id;
    private Integer fiscalYear;
    private BigDecimal totalProfit;
    private BigDecimal distributableProfit;
    private Long totalShares;
    private BigDecimal dividendPerShare;
    private BigDecimal whtRate;
    private String status;
    private String boardApprovedBy;
    private LocalDateTime boardApprovedAt;
    private LocalDateTime processedAt;
    private String notes;
    private Integer totalMembers;
    private BigDecimal totalDisbursed;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DividendAllocationResponse {
    private Long id;
    private Long cycleId;
    private Integer fiscalYear;
    private Long memberId;
    private String memberNumber;
    private String memberName;
    private Long sharesHeld;
    private BigDecimal grossDividend;
    private BigDecimal whtAmount;
    private BigDecimal netDividend;
    private String disbursementStatus;
    private LocalDateTime disbursedAt;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PreviewResponse {
    private Integer fiscalYear;
    private BigDecimal distributableProfit;
    private Long totalShares;
    private BigDecimal dividendPerShare;
    private BigDecimal whtRate;
    private List<DividendAllocationResponse> allocations;
    private BigDecimal totalGross;
    private BigDecimal totalWht;
    private BigDecimal totalNet;
    private Integer memberCount;
}

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DisbursementSummary {
    private Long cycleId;
    private Integer fiscalYear;
    private Integer totalAllocations;
    private Integer posted;
    private Integer failed;
    private Integer pending;
    private BigDecimal totalDisbursed;
}
