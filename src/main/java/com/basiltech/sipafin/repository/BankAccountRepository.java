package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BankAccount;
import com.basiltech.sipafin.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByAccountNumberIgnoreCase(String accountNumber);

    boolean existsByAccountNumberIgnoreCase(String accountNumber);

    boolean existsByAccountNumberIgnoreCaseAndIdNot(String accountNumber, Long id);

    List<BankAccount> findByActive(boolean active);

    List<BankAccount> findByCurrency(CurrencyCode currency);

    List<BankAccount> findByCurrencyAndActive(CurrencyCode currency, boolean active);

    List<BankAccount> findByBankId(Long bankId);

    List<BankAccount> findByBankIdAndCurrency(Long bankId, CurrencyCode currency);

    List<BankAccount> findByAccountNameContainingIgnoreCaseOrAccountNumberContainingIgnoreCase(
            String accountName,
            String accountNumber
    );
}