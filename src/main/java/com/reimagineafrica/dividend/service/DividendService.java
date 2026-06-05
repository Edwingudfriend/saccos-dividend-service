package com.reimagineafrica.dividend.service;

import com.reimagineafrica.dividend.client.FineractClient;
import com.reimagineafrica.dividend.dto.*;
import com.reimagineafrica.dividend.entity.DividendAllocation;
import com.reimagineafrica.dividend.entity.DividendAllocation.DisbursementStatus;
import com.reimagineafrica.dividend.entity.DividendCycle;
import com.reimagineafrica.dividend.entity.DividendCycle.CycleStatus;
import com.reimagineafrica.dividend.repository.DividendAllocationRepository;
import com.reimagineafrica.dividend.repository.DividendCycleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DividendService {

    private final DividendCycleRepository cycleRepo;
    private final DividendAllocationRepository allocationRepo;
    private final FineractClient fineractClient;
    private final RabbitTemplate rabbitTemplate;
    private final SharesServiceClient sharesClient;

    @Value("${tza.wht-rate}")
    private BigDecimal whtRate;

    // ── 1. Create a new dividend cycle ────────────────────────────────────────

    @Transactional
    public DividendCycleResponse createCycle(Integer fiscalYear,
                                              BigDecimal totalProfit,
                                              BigDecimal distributableProfit,
                                              String notes) {
        if (cycleRepo.existsByFiscalYear(fiscalYear)) {
            throw new IllegalStateException("Dividend cycle already exists for year " + fiscalYear);
        }

        DividendCycle cycle = DividendCycle.builder()
            .fiscalYear(fiscalYear)
            .totalProfit(totalProfit)
            .distributableProfit(distributableProfit)
            .whtRate(whtRate)
            .status(CycleStatus.PENDING)
            .notes(notes)
            .build();

        cycle = cycleRepo.save(cycle);
        log.info("Created dividend cycle for fiscal year {}", fiscalYear);
        return mapCycleToResponse(cycle);
    }

    // ── 2. Preview — simulate before board approval ───────────────────────────

    @Transactional(readOnly = true)
    public PreviewResponse previewDividends(Integer fiscalYear) {
        DividendCycle cycle = getCycleByYear(fiscalYear);

        // Fetch all members + their shares from shares service
        List<Map<String, Object>> members = sharesClient.getAllMembersWithShares();

        long totalShares = members.stream()
            .mapToLong(m -> ((Number) m.getOrDefault("sharesHeld", 0)).longValue())
            .sum();

        if (totalShares == 0) {
            throw new IllegalStateException("No shares found — cannot calculate dividends");
        }

        BigDecimal dividendPerShare = cycle.getDistributableProfit()
            .divide(BigDecimal.valueOf(totalShares), 6, RoundingMode.HALF_UP);

        List<DividendAllocationResponse> allocations = members.stream()
            .filter(m -> ((Number) m.getOrDefault("sharesHeld", 0)).longValue() > 0)
            .map(m -> {
                long shares = ((Number) m.get("sharesHeld")).longValue();
                BigDecimal gross = dividendPerShare.multiply(BigDecimal.valueOf(shares))
                    .setScale(2, RoundingMode.HALF_UP);
                BigDecimal wht = gross.multiply(whtRate).setScale(2, RoundingMode.HALF_UP);
                BigDecimal net = gross.subtract(wht);

                return DividendAllocationResponse.builder()
                    .memberId(((Number) m.get("memberId")).longValue())
                    .memberNumber((String) m.get("memberNumber"))
                    .memberName((String) m.get("memberName"))
                    .sharesHeld(shares)
                    .grossDividend(gross)
                    .whtAmount(wht)
                    .netDividend(net)
                    .disbursementStatus("PREVIEW")
                    .build();
            })
            .collect(Collectors.toList());

        BigDecimal totalGross = allocations.stream().map(DividendAllocationResponse::getGrossDividend)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWht = allocations.stream().map(DividendAllocationResponse::getWhtAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = allocations.stream().map(DividendAllocationResponse::getNetDividend)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return PreviewResponse.builder()
            .fiscalYear(fiscalYear)
            .distributableProfit(cycle.getDistributableProfit())
            .totalShares(totalShares)
            .dividendPerShare(dividendPerShare)
            .whtRate(whtRate)
            .allocations(allocations)
            .totalGross(totalGross)
            .totalWht(totalWht)
            .totalNet(totalNet)
            .memberCount(allocations.size())
            .build();
    }

    // ── 3. Board approval ─────────────────────────────────────────────────────

    @Transactional
    public DividendCycleResponse approveCycle(Integer fiscalYear, String approvedBy) {
        DividendCycle cycle = getCycleByYear(fiscalYear);

        if (cycle.getStatus() != CycleStatus.PENDING) {
            throw new IllegalStateException("Cycle is not in PENDING status");
        }

        cycle.setStatus(CycleStatus.BOARD_APPROVED);
        cycle.setBoardApprovedBy(approvedBy);
        cycle.setBoardApprovedAt(LocalDateTime.now());
        cycleRepo.save(cycle);

        log.info("Dividend cycle {} approved by {}", fiscalYear, approvedBy);

        // Notify via RabbitMQ
        rabbitTemplate.convertAndSend("dividend.exchange", "dividend.approved",
            Map.of("fiscalYear", fiscalYear, "approvedBy", approvedBy));

        return mapCycleToResponse(cycle);
    }

    // ── 4. Disburse dividends ─────────────────────────────────────────────────

    @Transactional
    public DisbursementSummary disburseDividends(Integer fiscalYear) {
        DividendCycle cycle = getCycleByYear(fiscalYear);

        if (cycle.getStatus() != CycleStatus.BOARD_APPROVED) {
            throw new IllegalStateException("Cycle must be BOARD_APPROVED before disbursement");
        }

        cycle.setStatus(CycleStatus.PROCESSING);
        cycleRepo.save(cycle);

        // Get preview to build allocations
        PreviewResponse preview = previewDividends(fiscalYear);

        // Recalculate and update cycle totals
        cycle.setTotalShares(preview.getTotalShares());
        cycle.setDividendPerShare(preview.getDividendPerShare());

        int posted = 0, failed = 0;
        BigDecimal totalDisbursed = BigDecimal.ZERO;

        for (DividendAllocationResponse alloc : preview.getAllocations()) {
            DividendAllocation allocation = DividendAllocation.builder()
                .cycle(cycle)
                .memberId(alloc.getMemberId())
                .memberNumber(alloc.getMemberNumber())
                .memberName(alloc.getMemberName())
                .sharesHeld(alloc.getSharesHeld())
                .grossDividend(alloc.getGrossDividend())
                .whtAmount(alloc.getWhtAmount())
                .netDividend(alloc.getNetDividend())
                .disbursementStatus(DisbursementStatus.PENDING)
                .build();

            // Post net dividend to Fineract savings account
            Long savingsId = sharesClient.getMemberSavingsAccountId(alloc.getMemberId());
            if (savingsId != null) {
                allocation.setFineractSavingsId(savingsId);
                boolean success = fineractClient.postDividendToSavings(
                    savingsId,
                    alloc.getNetDividend(),
                    "Dividend " + fiscalYear + " - Net after WHT"
                );

                if (success) {
                    allocation.setDisbursementStatus(DisbursementStatus.POSTED);
                    allocation.setDisbursedAt(LocalDateTime.now());
                    totalDisbursed = totalDisbursed.add(alloc.getNetDividend());
                    posted++;

                    // Send SMS notification via notification service
                    rabbitTemplate.convertAndSend("notification.exchange", "notification.sms",
                        Map.of(
                            "memberId", alloc.getMemberId(),
                            "message", String.format(
                                "Habari %s, gawio lako la mwaka %d ni TZS %.2f (baada ya kodi). Imewekwa kwenye akaunti yako.",
                                alloc.getMemberName(), fiscalYear, alloc.getNetDividend()
                            )
                        )
                    );
                } else {
                    allocation.setDisbursementStatus(DisbursementStatus.FAILED);
                    allocation.setFailureReason("Fineract API returned error");
                    failed++;
                }
            } else {
                allocation.setDisbursementStatus(DisbursementStatus.SKIPPED);
                allocation.setFailureReason("No savings account found");
                failed++;
            }

            allocationRepo.save(allocation);
        }

        cycle.setStatus(CycleStatus.COMPLETED);
        cycle.setProcessedAt(LocalDateTime.now());
        cycleRepo.save(cycle);

        log.info("Dividend disbursement completed for {}: posted={}, failed={}, total={}",
            fiscalYear, posted, failed, totalDisbursed);

        return DisbursementSummary.builder()
            .cycleId(cycle.getId())
            .fiscalYear(fiscalYear)
            .totalAllocations(preview.getMemberCount())
            .posted(posted)
            .failed(failed)
            .pending(0)
            .totalDisbursed(totalDisbursed)
            .build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private DividendCycle getCycleByYear(Integer fiscalYear) {
        return cycleRepo.findByFiscalYear(fiscalYear)
            .orElseThrow(() -> new RuntimeException("Dividend cycle not found for year " + fiscalYear));
    }

    private DividendCycleResponse mapCycleToResponse(DividendCycle cycle) {
        return DividendCycleResponse.builder()
            .id(cycle.getId())
            .fiscalYear(cycle.getFiscalYear())
            .totalProfit(cycle.getTotalProfit())
            .distributableProfit(cycle.getDistributableProfit())
            .totalShares(cycle.getTotalShares())
            .dividendPerShare(cycle.getDividendPerShare())
            .whtRate(cycle.getWhtRate())
            .status(cycle.getStatus().name())
            .boardApprovedBy(cycle.getBoardApprovedBy())
            .boardApprovedAt(cycle.getBoardApprovedAt())
            .processedAt(cycle.getProcessedAt())
            .notes(cycle.getNotes())
            .build();
    }
}
