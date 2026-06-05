package com.reimagineafrica.dividend.repository;

import com.reimagineafrica.dividend.entity.DividendAllocation;
import com.reimagineafrica.dividend.entity.DividendAllocation.DisbursementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DividendAllocationRepository extends JpaRepository<DividendAllocation, Long> {
    List<DividendAllocation> findByCycleId(Long cycleId);
    List<DividendAllocation> findByCycleIdAndDisbursementStatus(Long cycleId, DisbursementStatus status);
    List<DividendAllocation> findByMemberId(Long memberId);
}
