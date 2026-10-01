package com.basiltech.sipafin.repository;

import com.basiltech.sipafin.model.ClientLoanAccount;
import com.basiltech.sipafin.model.ClientLoanStatus;
import com.basiltech.sipafin.model.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClientLoanAccountRepository extends JpaRepository<ClientLoanAccount, Long> {

    List<ClientLoanAccount> findByBranchId(Long branchId);

    List<ClientLoanAccount> findByBranchIdAndCurrency(Long branchId, CurrencyCode currency);

    List<ClientLoanAccount> findByStatus(ClientLoanStatus status);

    List<ClientLoanAccount> findByClientNameContainingIgnoreCaseOrClientPhoneContainingIgnoreCase(
            String clientName,
            String clientPhone
    );
}