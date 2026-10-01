package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.BankReconciliationDtos;

import java.util.List;

public interface BankReconciliationService {

    BankReconciliationDtos.BankReconciliationResponse reconcile(
            BankReconciliationDtos.ReconcileBankAccountRequest request
    );

    List<BankReconciliationDtos.BankReconciliationResponse> getReconciliationsByBankAccount(Long bankAccountId);
}