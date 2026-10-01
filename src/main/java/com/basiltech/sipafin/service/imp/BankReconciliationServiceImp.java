package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.BankReconciliationDtos;
import com.basiltech.sipafin.mapper.BankReconciliationMapper;
import com.basiltech.sipafin.model.*;
import com.basiltech.sipafin.repository.BankAccountRepository;
import com.basiltech.sipafin.repository.BankReconciliationRepository;
import com.basiltech.sipafin.repository.BankTransactionRepository;
import com.basiltech.sipafin.repository.BranchRepository;
import com.basiltech.sipafin.service.BankReconciliationService;
import com.basiltech.sipafin.web.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankReconciliationServiceImp implements BankReconciliationService {

    private final BankAccountRepository bankAccountRepository;
    private final BranchRepository branchRepository;
    private final BankReconciliationRepository bankReconciliationRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final BankReconciliationMapper bankReconciliationMapper;

    @Override
    @Transactional
    public BankReconciliationDtos.BankReconciliationResponse reconcile(
            BankReconciliationDtos.ReconcileBankAccountRequest request
    ) {
        BankAccount bankAccount = bankAccountRepository.findById(request.bankAccountId())
                .orElseThrow(() -> new NotFoundException("Bank account not found"));

        Branch branch = branchRepository.findById(request.branchId())
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (!bankAccount.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Bank account currency does not match reconciliation currency");
        }

        BigDecimal systemBefore = bankAccount.getCurrentBalance();
        BigDecimal difference = request.statementBalance().subtract(systemBefore);

        boolean balanced = difference.compareTo(BigDecimal.ZERO) == 0;
        String groupId = balanced ? null : "REC-" + UUID.randomUUID();

        BigDecimal systemAfter = systemBefore;
        boolean adjustmentApplied = false;
        ReconciliationStatus status;

        if (balanced) {
            status = ReconciliationStatus.BALANCED;
        } else if (request.applyAdjustment()) {
            MoneyDirection direction = difference.compareTo(BigDecimal.ZERO) > 0
                    ? MoneyDirection.IN
                    : MoneyDirection.OUT;

            BigDecimal adjustmentAmount = difference.abs();

            systemAfter = request.statementBalance();

            bankAccount.setCurrentBalance(systemAfter);
            bankAccount.setActionedBy(request.actionedBy());
            bankAccountRepository.save(bankAccount);

            BankTransaction bankTransaction = new BankTransaction();
            bankTransaction.setBankAccount(bankAccount);
            bankTransaction.setBranch(branch);
            bankTransaction.setCurrency(bankAccount.getCurrency());
            bankTransaction.setTransactionType(BankLedgerTransactionType.RECONCILIATION_ADJUSTMENT);
            bankTransaction.setDirection(direction);
            bankTransaction.setAmount(adjustmentAmount);
            bankTransaction.setBalanceAfter(systemAfter);
            bankTransaction.setTransactionDate(request.reconciliationDate());
            bankTransaction.setTransactionGroupId(groupId);
            bankTransaction.setStatus(TransactionStatus.POSTED);
            bankTransaction.setReference(request.reference());
            bankTransaction.setDescription(request.reason());
            bankTransaction.setActionedBy(request.actionedBy());
            bankTransactionRepository.save(bankTransaction);

            adjustmentApplied = true;
            status = ReconciliationStatus.ADJUSTED;
        } else {
            status = ReconciliationStatus.UNRESOLVED;
        }

        BankReconciliation reconciliation = new BankReconciliation();
        reconciliation.setBankAccount(bankAccount);
        reconciliation.setBranch(branch);
        reconciliation.setCurrency(bankAccount.getCurrency());
        reconciliation.setReconciliationDate(request.reconciliationDate());
        reconciliation.setSystemBalanceBefore(systemBefore);
        reconciliation.setStatementBalance(request.statementBalance());
        reconciliation.setDifference(difference);
        reconciliation.setAdjustmentApplied(adjustmentApplied);
        reconciliation.setSystemBalanceAfter(systemAfter);
        reconciliation.setTransactionGroupId(groupId);
        reconciliation.setStatus(status);
        reconciliation.setReference(request.reference());
        reconciliation.setReason(request.reason());
        reconciliation.setActionedBy(request.actionedBy());

        return bankReconciliationMapper.toResponse(bankReconciliationRepository.save(reconciliation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliationDtos.BankReconciliationResponse> getReconciliationsByBankAccount(Long bankAccountId) {
        return bankReconciliationRepository.findByBankAccountId(bankAccountId)
                .stream()
                .sorted(Comparator.comparing(BankReconciliation::getId).reversed())
                .map(bankReconciliationMapper::toResponse)
                .toList();
    }
}
