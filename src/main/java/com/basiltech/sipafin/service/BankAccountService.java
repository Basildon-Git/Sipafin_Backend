package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.BankAccountDtos;

import java.util.List;

public interface BankAccountService {

    BankAccountDtos.BankAccountResponse createBankAccount(BankAccountDtos.CreateBankAccountRequest request);

    List<BankAccountDtos.BankAccountResponse> getAllBankAccounts();

    List<BankAccountDtos.BankAccountResponse> getActiveBankAccounts();

    List<BankAccountDtos.BankAccountResponse> getInactiveBankAccounts();

    List<BankAccountDtos.BankAccountResponse> getBankAccountsByBank(Long bankId);

    List<BankAccountDtos.BankAccountResponse> getBankAccountsByBranch(Long branchId);

    List<BankAccountDtos.BankAccountResponse> searchBankAccounts(String keyword);

    BankAccountDtos.BankAccountResponse getBankAccountById(Long id);

    BankAccountDtos.BankAccountResponse updateBankAccount(Long id, BankAccountDtos.UpdateBankAccountRequest request);

    BankAccountDtos.BankAccountResponse activateBankAccount(Long id, BankAccountDtos.BankAccountStatusRequest request);

    BankAccountDtos.BankAccountResponse deactivateBankAccount(Long id, BankAccountDtos.BankAccountStatusRequest request);
}
