package com.reimagineafrica.dividend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dividend_cycle")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DividendCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer fiscalYear;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal distributableProfit = BigDecimal.ZERO;

    @Column(nullable = false)
    private Long totalShares = 0L;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal dividendPerShare = BigDecimal.ZERO;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal whtRate = new BigDecimal("0.05");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CycleStatus status = CycleStatus.PENDING;

    private String boardApprovedBy;
    private LocalDateTime boardApprovedAt;
    private LocalDateTime processedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "cycle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DividendAllocation> allocations = new ArrayList<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum CycleStatus {
        PENDING, BOARD_APPROVED, PROCESSING, COMPLETED, CANCELLED
    }
}
