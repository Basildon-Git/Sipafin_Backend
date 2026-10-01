package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.BranchFloatDtos;
import com.basiltech.sipafin.model.CurrencyCode;

import java.util.List;

public interface BranchFloatService {

    BranchFloatDtos.BranchFloatAccountResponse createFloatAccount(
            BranchFloatDtos.CreateBranchFloatAccountRequest request
    );

    List<BranchFloatDtos.BranchFloatAccountResponse> getAllFloatAccounts();

    List<BranchFloatDtos.BranchFloatAccountResponse> getFloatAccountsByBranch(Long branchId);

    List<BranchFloatDtos.BranchFloatAccountResponse> getFloatAccountsByCurrency(CurrencyCode currency);

    BranchFloatDtos.BranchFloatAccountResponse getFloatAccountByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    );

    BranchFloatDtos.BranchFloatAccountResponse activateFloatAccount(
            Long id,
            BranchFloatDtos.BranchFloatStatusRequest request
    );

    BranchFloatDtos.BranchFloatAccountResponse deactivateFloatAccount(
            Long id,
            BranchFloatDtos.BranchFloatStatusRequest request
    );
}