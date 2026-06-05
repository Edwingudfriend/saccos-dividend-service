package com.reimagineafrica.dividend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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
