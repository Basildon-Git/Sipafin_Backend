package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.BankAccountDtos;
import com.basiltech.sipafin.mapper.BankAccountMapper;
import com.basiltech.sipafin.model.Bank;
import com.basiltech.sipafin.model.BankAccount;
import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.repository.BankAccountRepository;
import com.basiltech.sipafin.repository.BankRepository;
import com.basiltech.sipafin.service.BankAccountService;
import com.basiltech.sipafin.web.ConflictException;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImp implements BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final BankRepository bankRepository;
    private final BankAccountMapper bankAccountMapper;

    @Override
    @Transactional
    public BankAccountDtos.BankAccountResponse createBankAccount(BankAccountDtos.CreateBankAccountRequest request) {
        if (bankAccountRepository.existsByAccountNumberIgnoreCase(request.accountNumber().trim())) {
            throw new ConflictException("Bank account number already exists");
        }

        Bank bank = findActiveBank(request.bankId());

        BankAccount bankAccount = new BankAccount();
        bankAccount.setBank(bank);
        bankAccount.setAccountName(request.accountName().trim());
        bankAccount.setAccountNumber(request.accountNumber().trim());
        bankAccount.setCurrency(request.currency());
        bankAccount.setCurrentBalance(request.openingBalance());
        bankAccount.setActive(true);
        bankAccount.setActionedBy(request.actionedBy());

        return bankAccountMapper.toResponse(bankAccountRepository.save(bankAccount));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> getAllBankAccounts() {
        return bankAccountRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> getActiveBankAccounts() {
        return bankAccountRepository.findByActive(true)
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> getInactiveBankAccounts() {
        return bankAccountRepository.findByActive(false)
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> getBankAccountsByBank(Long bankId) {
        return bankAccountRepository.findByBankId(bankId)
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> getBankAccountsByCurrency(CurrencyCode currency) {
        return bankAccountRepository.findByCurrency(currency)
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> getActiveBankAccountsByCurrency(CurrencyCode currency) {
        return bankAccountRepository.findByCurrencyAndActive(currency, true)
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> getBankAccountsByBankAndCurrency(Long bankId, CurrencyCode currency) {
        return bankAccountRepository.findByBankIdAndCurrency(bankId, currency)
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankAccountDtos.BankAccountResponse> searchBankAccounts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllBankAccounts();
        }

        return bankAccountRepository
                .findByAccountNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCase(keyword, keyword)
                .stream()
                .sorted(Comparator.comparing(BankAccount::getId).reversed())
                .map(bankAccountMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BankAccountDtos.BankAccountResponse getBankAccountById(Long id) {
        return bankAccountMapper.toResponse(findBankAccount(id));
    }

    @Override
    @Transactional
    public BankAccountDtos.BankAccountResponse updateBankAccount(Long id, BankAccountDtos.UpdateBankAccountRequest request) {
        BankAccount bankAccount = findBankAccount(id);

        if (bankAccountRepository.existsByAccountNumberIgnoreCaseAndIdNot(request.accountNumber().trim(), id)) {
            throw new ConflictException("Bank account number already exists");
        }

        Bank bank = findActiveBank(request.bankId());

        bankAccount.setBank(bank);
        bankAccount.setAccountName(request.accountName().trim());
        bankAccount.setAccountNumber(request.accountNumber().trim());
        bankAccount.setCurrency(request.currency());
        bankAccount.setActive(request.active());
        bankAccount.setActionedBy(request.actionedBy());

        return bankAccountMapper.toResponse(bankAccountRepository.save(bankAccount));
    }

    @Override
    @Transactional
    public BankAccountDtos.BankAccountResponse activateBankAccount(Long id, BankAccountDtos.BankAccountStatusRequest request) {
        BankAccount bankAccount = findBankAccount(id);
        bankAccount.setActive(true);
        bankAccount.setActionedBy(request.actionedBy());
        return bankAccountMapper.toResponse(bankAccountRepository.save(bankAccount));
    }

    @Override
    @Transactional
    public BankAccountDtos.BankAccountResponse deactivateBankAccount(Long id, BankAccountDtos.BankAccountStatusRequest request) {
        BankAccount bankAccount = findBankAccount(id);
        bankAccount.setActive(false);
        bankAccount.setActionedBy(request.actionedBy());
        return bankAccountMapper.toResponse(bankAccountRepository.save(bankAccount));
    }

    private BankAccount findBankAccount(Long id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank account not found"));
    }

    private Bank findActiveBank(Long bankId) {
        Bank bank = bankRepository.findById(bankId)
                .orElseThrow(() -> new NotFoundException("Bank not found"));

        if (!bank.isActive()) {
            throw new IllegalArgumentException("Bank is inactive");
        }

        return bank;
    }
}