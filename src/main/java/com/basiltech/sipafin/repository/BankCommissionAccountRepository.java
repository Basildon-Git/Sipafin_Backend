package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.BankCommissionAccount;
import com.basiltech.sipafin.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankCommissionAccountRepository extends JpaRepository<BankCommissionAccount, Long> {

    Optional<BankCommissionAccount> findByBankAccountId(Long bankAccountId);

    Optional<BankCommissionAccount> findByBankAccountIdAndActive(Long bankAccountId, boolean active);

    boolean existsByBankAccountId(Long bankAccountId);

    List<BankCommissionAccount> findByCurrency(CurrencyCode currency);

    List<BankCommissionAccount> findByActive(boolean active);
}