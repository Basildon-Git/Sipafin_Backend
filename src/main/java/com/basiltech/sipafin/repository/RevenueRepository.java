package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.model.Revenue;
import com.basiltech.sipafin.model.RevenueCategory;
import com.basiltech.sipafin.model.RevenueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RevenueRepository extends JpaRepository<Revenue, Long> {

    List<Revenue> findByBranchId(Long branchId);

    List<Revenue> findByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    List<Revenue> findByBranchIdAndCurrencyAndRevenueDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate revenueDate
    );

    List<Revenue> findByCategory(RevenueCategory category);

    List<Revenue> findByStatus(RevenueStatus status);

    List<Revenue> findByRevenueDateBetween(LocalDate fromDate, LocalDate toDate);
}