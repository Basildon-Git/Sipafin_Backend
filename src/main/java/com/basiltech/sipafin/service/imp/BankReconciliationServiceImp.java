package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.BankReconciliationDtos;
import com.basiltech.sipafin.model.*;
import com.basiltech.sipafin.repository.*;
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
    private final BankReconciliationItemRepository bankReconciliationItemRepository;
    private final BankTransactionRepository bankTransactionRepository;

    @Override
    @Transactional
    public BankReconciliationDtos.BankReconciliationResponse startReconciliation(
            BankReconciliationDtos.StartBankReconciliationRequest request
    ) {
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId());
        Branch branch = resolveActiveBranch(request.branchId());

        if (!bankAccount.getCurrency().equals(request.currency())) {
            throw new IllegalArgumentException("Bank account currency does not match reconciliation currency");
        }

        BigDecimal systemBalance = bankAccount.getCurrentBalance();
        BigDecimal difference = request.statementBalance().subtract(systemBalance);
        ReconciliationDifferenceType differenceType = resolveDifferenceType(difference);
        BigDecimal unreconciledAmount = difference.abs();

        BankReconciliation reconciliation = new BankReconciliation();
        reconciliation.setBankAccount(bankAccount);
        reconciliation.setBranch(branch);
        reconciliation.setCurrency(bankAccount.getCurrency());
        reconciliation.setReconciliationDate(request.reconciliationDate());
        reconciliation.setSystemBalanceBefore(systemBalance);
        reconciliation.setStatementBalance(request.statementBalance());
        reconciliation.setDifference(difference);
        reconciliation.setDifferenceType(differenceType);
        reconciliation.setReconciledAmount(BigDecimal.ZERO);
        reconciliation.setUnreconciledAmount(unreconciledAmount);
        reconciliation.setSystemBalanceAfter(systemBalance);
        reconciliation.setTransactionGroupId("REC-" + UUID.randomUUID());
        reconciliation.setStatus(differenceType == ReconciliationDifferenceType.BALANCED
                ? ReconciliationStatus.BALANCED
                : ReconciliationStatus.UNRESOLVED);
        reconciliation.setReference(request.reference());
        reconciliation.setReason(request.reason());
        reconciliation.setActionedBy(request.actionedBy());

        return toResponse(bankReconciliationRepository.save(reconciliation));
    }

    @Override
    @Transactional
    public BankReconciliationDtos.BankReconciliationResponse addReconciliationItem(
            Long reconciliationId,
            BankReconciliationDtos.AddReconciliationItemRequest request
    ) {
        BankReconciliation reconciliation = findReconciliation(reconciliationId);

        if (reconciliation.getStatus() == ReconciliationStatus.BALANCED) {
            throw new IllegalArgumentException("Reconciliation is already balanced");
        }

        if (reconciliation.getStatus() == ReconciliationStatus.CANCELLED) {
            throw new IllegalArgumentException("Reconciliation is cancelled");
        }

        if (request.amount().compareTo(reconciliation.getUnreconciledAmount()) > 0) {
            throw new IllegalArgumentException("Reconciliation item amount cannot exceed unreconciled amount");
        }

        validateItemEffect(reconciliation, request.effect());

        BankTransaction bankTransaction = null;

        if (request.effect() != ReconciliationItemEffect.NO_BALANCE_EFFECT) {
            bankTransaction = postBankReconciliationTransaction(reconciliation, request);
        }

        BankReconciliationItem item = new BankReconciliationItem();
        item.setReconciliation(reconciliation);
        item.setCategory(request.category());
        item.setEffect(request.effect());
        item.setAmount(request.amount());
        item.setTransactionDate(request.transactionDate());
        item.setBankTransaction(bankTransaction);
        item.setReference(request.reference());
        item.setDescription(request.description());
        item.setActionedBy(request.actionedBy());
        bankReconciliationItemRepository.save(item);

        BigDecimal reconciledAmount = reconciliation.getReconciledAmount().add(request.amount());
        BigDecimal unreconciledAmount = reconciliation.getDifference().abs().subtract(reconciledAmount);

        reconciliation.setReconciledAmount(reconciledAmount);
        reconciliation.setUnreconciledAmount(unreconciledAmount);
        reconciliation.setSystemBalanceAfter(reconciliation.getBankAccount().getCurrentBalance());
        reconciliation.setActionedBy(request.actionedBy());

        if (unreconciledAmount.compareTo(BigDecimal.ZERO) == 0) {
            reconciliation.setStatus(ReconciliationStatus.BALANCED);
        } else {
            reconciliation.setStatus(ReconciliationStatus.PARTIALLY_RECONCILED);
        }

        return toResponse(bankReconciliationRepository.save(reconciliation));
    }

    @Override
    @Transactional(readOnly = true)
    public BankReconciliationDtos.BankReconciliationResponse getReconciliationById(Long id) {
        return toResponse(findReconciliation(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliationDtos.BankReconciliationResponse> getReconciliationsByBankAccount(Long bankAccountId) {
        return bankReconciliationRepository.findByBankAccountId(bankAccountId)
                .stream()
                .sorted(Comparator.comparing(BankReconciliation::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliationDtos.BankReconciliationItemResponse> getReconciliationItems(Long reconciliationId) {
        return bankReconciliationItemRepository.findByReconciliationId(reconciliationId)
                .stream()
                .sorted(Comparator.comparing(BankReconciliationItem::getId).reversed())
                .map(this::toItemResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BankReconciliationDtos.BankReconciliationResponse> getAllReconciliations() {
        return bankReconciliationRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BankReconciliation::getId).reversed())
                .map(this::toResponse)
                .toList();
    }

    private BankTransaction postBankReconciliationTransaction(
            BankReconciliation reconciliation,
            BankReconciliationDtos.AddReconciliationItemRequest request
    ) {
        BankAccount bankAccount = reconciliation.getBankAccount();

        MoneyDirection direction = request.effect() == ReconciliationItemEffect.BANK_IN
                ? MoneyDirection.IN
                : MoneyDirection.OUT;

        BigDecimal balanceAfter = direction == MoneyDirection.IN
                ? bankAccount.getCurrentBalance().add(request.amount())
                : bankAccount.getCurrentBalance().subtract(request.amount());

        bankAccount.setCurrentBalance(balanceAfter);
        bankAccount.setActionedBy(request.actionedBy());
        bankAccountRepository.save(bankAccount);

        BankTransaction transaction = new BankTransaction();
        transaction.setBankAccount(bankAccount);
        transaction.setBranch(reconciliation.getBranch());
        transaction.setCurrency(reconciliation.getCurrency());
        transaction.setTransactionType(BankLedgerTransactionType.RECONCILIATION_ADJUSTMENT);
        transaction.setDirection(direction);
        transaction.setAmount(request.amount());
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionDate(request.transactionDate());
        transaction.setTransactionGroupId(reconciliation.getTransactionGroupId());
        transaction.setStatus(TransactionStatus.POSTED);
        transaction.setReference(request.reference());
        transaction.setDescription(resolveTransactionDescription(request));
        transaction.setActionedBy(request.actionedBy());

        return bankTransactionRepository.save(transaction);
    }

    private String resolveTransactionDescription(BankReconciliationDtos.AddReconciliationItemRequest request) {
        if (request.description() != null && !request.description().isBlank()) {
            return request.description();
        }

        return "Bank reconciliation item: " + request.category();
    }

    private void validateItemEffect(BankReconciliation reconciliation, ReconciliationItemEffect effect) {
        if (reconciliation.getDifferenceType() == ReconciliationDifferenceType.SHORT
                && effect == ReconciliationItemEffect.BANK_IN) {
            throw new IllegalArgumentException("SHORT reconciliation expects BANK_OUT or NO_BALANCE_EFFECT");
        }

        if (reconciliation.getDifferenceType() == ReconciliationDifferenceType.EXCESS
                && effect == ReconciliationItemEffect.BANK_OUT) {
            throw new IllegalArgumentException("EXCESS reconciliation expects BANK_IN or NO_BALANCE_EFFECT");
        }

        if (reconciliation.getDifferenceType() == ReconciliationDifferenceType.BALANCED
                && effect != ReconciliationItemEffect.NO_BALANCE_EFFECT) {
            throw new IllegalArgumentException("Balanced reconciliation does not need bank balance movement");
        }
    }

    private ReconciliationDifferenceType resolveDifferenceType(BigDecimal difference) {
        if (difference.compareTo(BigDecimal.ZERO) == 0) {
            return ReconciliationDifferenceType.BALANCED;
        }

        if (difference.compareTo(BigDecimal.ZERO) < 0) {
            return ReconciliationDifferenceType.SHORT;
        }

        return ReconciliationDifferenceType.EXCESS;
    }

    private BankReconciliation findReconciliation(Long id) {
        return bankReconciliationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank reconciliation not found"));
    }

    private BankAccount findActiveBankAccount(Long bankAccountId) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new NotFoundException("Bank account not found"));

        if (!bankAccount.isActive()) {
            throw new IllegalArgumentException("Bank account is inactive");
        }

        if (!bankAccount.getBank().isActive()) {
            throw new IllegalArgumentException("Bank is inactive");
        }

        return bankAccount;
    }

    private Branch findActiveBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        return branch;
    }

    private BankReconciliationDtos.BankReconciliationResponse toResponse(BankReconciliation reconciliation) {
        BankAccount bankAccount = reconciliation.getBankAccount();
        Branch branch = reconciliation.getBranch();

        List<BankReconciliationDtos.BankReconciliationItemResponse> items =
                bankReconciliationItemRepository.findByReconciliationId(reconciliation.getId())
                        .stream()
                        .sorted(Comparator.comparing(BankReconciliationItem::getId).reversed())
                        .map(this::toItemResponse)
                        .toList();

        return new BankReconciliationDtos.BankReconciliationResponse(
                reconciliation.getId(),
                bankAccount.getId(),
                bankAccount.getBank().getName(),
                bankAccount.getBank().getCode(),
                bankAccount.getAccountName(),
                bankAccount.getAccountNumber(),
                branch == null ? null : branch.getId(),
                branch == null ? null : branch.getName(),
                reconciliation.getCurrency(),
                reconciliation.getReconciliationDate(),
                reconciliation.getSystemBalanceBefore(),
                reconciliation.getStatementBalance(),
                reconciliation.getDifference(),
                reconciliation.getDifferenceType(),
                reconciliation.getReconciledAmount(),
                reconciliation.getUnreconciledAmount(),
                reconciliation.getSystemBalanceAfter(),
                reconciliation.getTransactionGroupId(),
                reconciliation.getStatus(),
                reconciliation.getReference(),
                reconciliation.getReason(),
                reconciliation.getActionedBy(),
                reconciliation.getCreatedAt(),
                reconciliation.getUpdatedAt(),
                items
        );
    }

    private BankReconciliationDtos.BankReconciliationItemResponse toItemResponse(BankReconciliationItem item) {
        return new BankReconciliationDtos.BankReconciliationItemResponse(
                item.getId(),
                item.getReconciliation().getId(),
                item.getCategory(),
                item.getEffect(),
                item.getAmount(),
                item.getTransactionDate(),
                item.getBankTransaction() == null ? null : item.getBankTransaction().getId(),
                item.getReference(),
                item.getDescription(),
                item.getActionedBy(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

    private Branch resolveActiveBranch(Long branchId) {
        if (branchId == null || branchId <= 0) {
            return null;
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        return branch;
    }


}