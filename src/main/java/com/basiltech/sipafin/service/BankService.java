package com.basiltech.sipafin.service;

import com.basiltech.sipafin.dto.BankDtos;

import java.util.List;

public interface BankService {

    BankDtos.BankResponse createBank(BankDtos.CreateBankRequest request);

    List<BankDtos.BankResponse> getAllBanks();

    List<BankDtos.BankResponse> getActiveBanks();

    List<BankDtos.BankResponse> getInactiveBanks();

    List<BankDtos.BankResponse> searchBanks(String keyword);

    BankDtos.BankResponse getBankById(Long id);

    BankDtos.BankResponse updateBank(Long id, BankDtos.UpdateBankRequest request);

    BankDtos.BankResponse activateBank(Long id, BankDtos.BankStatusRequest request);

    BankDtos.BankResponse deactivateBank(Long id, BankDtos.BankStatusRequest request);
}