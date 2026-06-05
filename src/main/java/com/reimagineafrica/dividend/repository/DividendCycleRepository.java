package com.reimagineafrica.dividend.repository;

import com.reimagineafrica.dividend.entity.DividendCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DividendCycleRepository extends JpaRepository<DividendCycle, Long> {
    Optional<DividendCycle> findByFiscalYear(Integer fiscalYear);
    boolean existsByFiscalYear(Integer fiscalYear);
}
