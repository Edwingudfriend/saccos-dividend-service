package com.reimagineafrica.dividend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dividend_allocation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DividendAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private DividendCycle cycle;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false, length = 30)
    private String memberNumber;

    @Column(nullable = false, length = 200)
    private String memberName;

    @Column(nullable = false)
    private Long sharesHeld = 0L;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal grossDividend = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal whtAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal netDividend = BigDecimal.ZERO;

    private Long fineractSavingsId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisbursementStatus disbursementStatus = DisbursementStatus.PENDING;

    private LocalDateTime disbursedAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum DisbursementStatus {
        PENDING, POSTED, FAILED, SKIPPED
    }
}
