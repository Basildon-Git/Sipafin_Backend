package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.CommissionDtos;
import com.basiltech.sipafin.model.CurrencyCode;

import java.time.LocalDate;
import java.util.List;

public interface CommissionService {

    CommissionDtos.BankCommissionAccountResponse createCommissionAccount(
            CommissionDtos.CreateBankCommissionAccountRequest request
    );

    CommissionDtos.BankCommissionAccountResponse updateCommissionAccount(
            Long id,
            CommissionDtos.UpdateBankCommissionAccountRequest request
    );

    List<CommissionDtos.BankCommissionAccountResponse> getAllCommissionAccounts();

    List<CommissionDtos.BankCommissionAccountResponse> getCommissionAccountsByCurrency(CurrencyCode currency);

    CommissionDtos.BankCommissionAccountResponse getCommissionAccountByBankAccount(Long bankAccountId);

    List<CommissionDtos.CommissionResponse> getCommissionsByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    );

    List<CommissionDtos.CommissionResponse> getCommissionsByBranchCurrencyAndDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate commissionDate
    );
}