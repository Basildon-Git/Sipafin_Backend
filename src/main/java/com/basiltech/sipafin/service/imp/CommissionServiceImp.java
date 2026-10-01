package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.CommissionDtos;
import com.basiltech.sipafin.mapper.CommissionMapper;
import com.basiltech.sipafin.model.BankAccount;
import com.basiltech.sipafin.model.BankCommissionAccount;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.repository.BankAccountRepository;
import com.basiltech.sipafin.repository.BankCommissionAccountRepository;
import com.basiltech.sipafin.repository.CommissionRepository;
import com.basiltech.sipafin.service.CommissionService;
import com.basiltech.sipafin.web.ConflictException;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommissionServiceImp implements CommissionService {

    private final BankAccountRepository bankAccountRepository;
    private final BankCommissionAccountRepository bankCommissionAccountRepository;
    private final CommissionRepository commissionRepository;
    private final CommissionMapper commissionMapper;

    @Override
    @Transactional
    public CommissionDtos.BankCommissionAccountResponse createCommissionAccount(
            CommissionDtos.CreateBankCommissionAccountRequest request
    ) {
        if (bankCommissionAccountRepository.existsByBankAccountId(request.bankAccountId())) {
            throw new ConflictException("Commission account already exists for this bank account");
        }

        BankAccount bankAccount = bankAccountRepository.findById(request.bankAccountId())
                .orElseThrow(() -> new NotFoundException("Bank account not found"));

        BankCommissionAccount account = new BankCommissionAccount();
        account.setBankAccount(bankAccount);
        account.setCurrency(bankAccount.getCurrency());
        account.setCommissionRate(request.commissionRate());
        account.setActive(true);
        account.setActionedBy(request.actionedBy());

        return commissionMapper.toCommissionAccountResponse(bankCommissionAccountRepository.save(account));
    }

    @Override
    @Transactional
    public CommissionDtos.BankCommissionAccountResponse updateCommissionAccount(
            Long id,
            CommissionDtos.UpdateBankCommissionAccountRequest request
    ) {
        BankCommissionAccount account = bankCommissionAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Commission account not found"));

        account.setCommissionRate(request.commissionRate());
        account.setActive(request.active());
        account.setActionedBy(request.actionedBy());

        return commissionMapper.toCommissionAccountResponse(bankCommissionAccountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionDtos.BankCommissionAccountResponse> getAllCommissionAccounts() {
        return bankCommissionAccountRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BankCommissionAccount::getId).reversed())
                .map(commissionMapper::toCommissionAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionDtos.BankCommissionAccountResponse> getCommissionAccountsByCurrency(CurrencyCode currency) {
        return bankCommissionAccountRepository.findByCurrency(currency)
                .stream()
                .sorted(Comparator.comparing(BankCommissionAccount::getId).reversed())
                .map(commissionMapper::toCommissionAccountResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionDtos.BankCommissionAccountResponse getCommissionAccountByBankAccount(Long bankAccountId) {
        BankCommissionAccount account = bankCommissionAccountRepository.findByBankAccountId(bankAccountId)
                .orElseThrow(() -> new NotFoundException("Commission account not found for bank account"));

        return commissionMapper.toCommissionAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionDtos.CommissionResponse> getCommissionsByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    ) {
        return commissionRepository.findByBranchIdAndCurrency(branchId, currency)
                .stream()
                .map(commissionMapper::toCommissionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionDtos.CommissionResponse> getCommissionsByBranchCurrencyAndDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate commissionDate
    ) {
        return commissionRepository.findByBranchIdAndCurrencyAndCommissionDate(branchId, currency, commissionDate)
                .stream()
                .map(commissionMapper::toCommissionResponse)
                .toList();
    }
}