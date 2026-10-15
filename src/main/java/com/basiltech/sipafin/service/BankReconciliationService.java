package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.BankReconciliationDtos;

import java.util.List;

public interface BankReconciliationService {

    BankReconciliationDtos.BankReconciliationResponse startReconciliation(
            BankReconciliationDtos.StartBankReconciliationRequest request
    );

    BankReconciliationDtos.BankReconciliationResponse addReconciliationItem(
            Long reconciliationId,
            BankReconciliationDtos.AddReconciliationItemRequest request
    );

    BankReconciliationDtos.BankReconciliationResponse getReconciliationById(Long id);

    List<BankReconciliationDtos.BankReconciliationResponse> getAllReconciliations();

    List<BankReconciliationDtos.BankReconciliationResponse> getReconciliationsByBankAccount(Long bankAccountId);

    List<BankReconciliationDtos.BankReconciliationItemResponse> getReconciliationItems(Long reconciliationId);
}