package com.reimagineafrica.dividend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

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
