package com.reimagineafrica.dividend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
