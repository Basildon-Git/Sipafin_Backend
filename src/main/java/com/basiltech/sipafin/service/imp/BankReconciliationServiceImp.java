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
import java.time.LocalDate;
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

        BigDecimal systemClosingBalance = resolveSystemClosingBalance(
                bankAccount,
                request.reconciliationDate()
        );

        BigDecimal difference = request.statementBalance().subtract(systemClosingBalance);
        ReconciliationDifferenceType differenceType = resolveDifferenceType(difference);
        BigDecimal reconciledAmount = BigDecimal.ZERO;
        BigDecimal unreconciledAmount = difference.abs();
        BigDecimal systemBalanceAfterReconciliation = systemClosingBalance;

        BankReconciliation reconciliation = new BankReconciliation();
        reconciliation.setBankAccount(bankAccount);
        reconciliation.setBranch(branch);
        reconciliation.setCurrency(bankAccount.getCurrency());
        reconciliation.setReconciliationDate(request.reconciliationDate());
        reconciliation.setSystemBalanceBefore(systemClosingBalance);
        reconciliation.setStatementBalance(request.statementBalance());
        reconciliation.setDifference(difference);
        reconciliation.setDifferenceType(differenceType);
        reconciliation.setReconciledAmount(reconciledAmount);
        reconciliation.setUnreconciledAmount(unreconciledAmount);
        reconciliation.setSystemBalanceAfter(systemBalanceAfterReconciliation);
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

        ReconciliationSnapshot snapshotBeforeItem = resolveReconciliationSnapshot(reconciliation);

        if (request.amount().compareTo(snapshotBeforeItem.unreconciledAmount()) > 0) {
            throw new IllegalArgumentException("Reconciliation item amount cannot exceed unreconciled amount");
        }

        validateItemEffect(snapshotBeforeItem.differenceType(), request.effect());

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

        ReconciliationSnapshot snapshotAfterItem = resolveReconciliationSnapshot(reconciliation);

        reconciliation.setSystemBalanceBefore(snapshotAfterItem.systemClosingBalance());
        reconciliation.setDifference(snapshotAfterItem.difference());
        reconciliation.setDifferenceType(snapshotAfterItem.differenceType());
        reconciliation.setReconciledAmount(snapshotAfterItem.reconciledAmount());
        reconciliation.setUnreconciledAmount(snapshotAfterItem.unreconciledAmount());
        reconciliation.setSystemBalanceAfter(snapshotAfterItem.systemBalanceAfterReconciliation());
        reconciliation.setActionedBy(request.actionedBy());

        if (snapshotAfterItem.unreconciledAmount().compareTo(BigDecimal.ZERO) == 0) {
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

        if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Reconciliation adjustment would make bank account balance negative");
        }

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

    private void validateItemEffect(
            ReconciliationDifferenceType differenceType,
            ReconciliationItemEffect effect
    ) {
        if (differenceType == ReconciliationDifferenceType.SHORT
                && effect == ReconciliationItemEffect.BANK_IN) {
            throw new IllegalArgumentException("SHORT reconciliation expects BANK_OUT or NO_BALANCE_EFFECT");
        }

        if (differenceType == ReconciliationDifferenceType.EXCESS
                && effect == ReconciliationItemEffect.BANK_OUT) {
            throw new IllegalArgumentException("EXCESS reconciliation expects BANK_IN or NO_BALANCE_EFFECT");
        }

        if (differenceType == ReconciliationDifferenceType.BALANCED
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

    private ReconciliationSnapshot resolveReconciliationSnapshot(BankReconciliation reconciliation) {
        BigDecimal systemClosingBalance = resolveSystemClosingBalance(
                reconciliation.getBankAccount(),
                reconciliation.getReconciliationDate()
        );

        BigDecimal difference = reconciliation.getStatementBalance().subtract(systemClosingBalance);
        ReconciliationDifferenceType differenceType = resolveDifferenceType(difference);

        List<BankReconciliationItem> items = bankReconciliationItemRepository
                .findByReconciliationId(reconciliation.getId());

        BigDecimal reconciledAmount = items.stream()
                .map(BankReconciliationItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unreconciledAmount = difference.abs().subtract(reconciledAmount);

        if (unreconciledAmount.compareTo(BigDecimal.ZERO) < 0) {
            unreconciledAmount = BigDecimal.ZERO;
        }

        BigDecimal systemBalanceAfterReconciliation = resolveSystemBalanceAfterReconciliation(
                systemClosingBalance,
                items
        );

        return new ReconciliationSnapshot(
                systemClosingBalance,
                difference,
                differenceType,
                reconciledAmount,
                unreconciledAmount,
                systemBalanceAfterReconciliation
        );
    }

    private BigDecimal resolveSystemClosingBalance(BankAccount bankAccount, LocalDate reconciliationDate) {
        return bankTransactionRepository
                .findByBankAccountIdAndCurrencyAndTransactionDateLessThanEqualAndStatus(
                        bankAccount.getId(),
                        bankAccount.getCurrency(),
                        reconciliationDate,
                        TransactionStatus.POSTED
                )
                .stream()
                .sorted(
                        Comparator.comparing(BankTransaction::getTransactionDate)
                                .thenComparing(BankTransaction::getId)
                                .reversed()
                )
                .map(BankTransaction::getBalanceAfter)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal resolveSystemBalanceAfterReconciliation(
            BigDecimal systemClosingBalance,
            List<BankReconciliationItem> items
    ) {
        BigDecimal balance = systemClosingBalance;

        List<BankReconciliationItem> sortedItems = items.stream()
                .sorted(Comparator.comparing(BankReconciliationItem::getId))
                .toList();

        for (BankReconciliationItem item : sortedItems) {
            if (item.getEffect() == ReconciliationItemEffect.BANK_IN) {
                balance = balance.add(item.getAmount());
            }

            if (item.getEffect() == ReconciliationItemEffect.BANK_OUT) {
                balance = balance.subtract(item.getAmount());
            }
        }

        return balance;
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

        ReconciliationSnapshot snapshot = resolveReconciliationSnapshot(reconciliation);

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
                snapshot.systemClosingBalance(),
                reconciliation.getStatementBalance(),
                snapshot.difference(),
                snapshot.differenceType(),
                snapshot.reconciledAmount(),
                snapshot.unreconciledAmount(),
                snapshot.systemBalanceAfterReconciliation(),
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

    private record ReconciliationSnapshot(
            BigDecimal systemClosingBalance,
            BigDecimal difference,
            ReconciliationDifferenceType differenceType,
            BigDecimal reconciledAmount,
            BigDecimal unreconciledAmount,
            BigDecimal systemBalanceAfterReconciliation
    ) {
    }
}