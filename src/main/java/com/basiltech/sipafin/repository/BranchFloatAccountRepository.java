package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BranchFloatAccount;
import com.basiltech.sipafin.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchFloatAccountRepository extends JpaRepository<BranchFloatAccount, Long> {

    Optional<BranchFloatAccount> findByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    boolean existsByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    List<BranchFloatAccount> findByBranchId(Long branchId);

    List<BranchFloatAccount> findByCurrency(CurrencyCode currency);

    List<BranchFloatAccount> findByActive(boolean active);

    List<BranchFloatAccount> findByCurrencyAndActive(CurrencyCode currency, boolean active);
}