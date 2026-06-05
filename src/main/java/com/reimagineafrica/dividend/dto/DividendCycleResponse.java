package com.reimagineafrica.dividend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
