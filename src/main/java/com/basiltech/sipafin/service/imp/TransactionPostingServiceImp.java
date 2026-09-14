package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.TransactionDtos;
import com.basiltech.sipafin.mapper.TransactionMapper;
import com.basiltech.sipafin.model.*;
import com.basiltech.sipafin.repository.BankAccountRepository;
import com.basiltech.sipafin.repository.BankTransactionRepository;
import com.basiltech.sipafin.repository.BranchRepository;
import com.basiltech.sipafin.repository.PettyCashTransactionRepository;
import com.basiltech.sipafin.service.TransactionPostingService;
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
public class TransactionPostingServiceImp implements TransactionPostingService {

    private final BranchRepository branchRepository;
    private final BankAccountRepository bankAccountRepository;
    private final PettyCashTransactionRepository pettyCashTransactionRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDtos.CashTransactionResponse openCashFloat(TransactionDtos.CashOpeningBalanceRequest request) {
        Branch branch = findActiveBranch(request.branchId());
        String groupId = generateGroupId();

        PettyCashTransaction cashTransaction = createCashTransaction(
                branch,
                PettyCashTransactionType.OPENING_BALANCE,
                CashDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        increaseBranchCash(branch, request.amount(), request.actionedBy());

        return transactionMapper.toCashResponse(cashTransaction);
    }

    @Override
    @Transactional
    public TransactionDtos.TransactionGroupResponse depositCashToBank(TransactionDtos.BankDepositRequest request) {
        Branch branch = findActiveBranch(request.branchId());
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId());
        String groupId = generateGroupId();

        ensureBranchHasEnoughCash(branch, request.amount());

        PettyCashTransaction cashTransaction = createCashTransaction(
                branch,
                PettyCashTransactionType.BANK_DEPOSIT,
                CashDirection.OUT,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        BankTransaction bankTransaction = createBankTransaction(
                bankAccount,
                branch,
                BankTransactionType.CASH_DEPOSIT,
                CashDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        decreaseBranchCash(branch, request.amount(), request.actionedBy());
        increaseBankBalance(bankAccount, request.amount(), request.actionedBy());

        return new TransactionDtos.TransactionGroupResponse(
                groupId,
                transactionMapper.toCashResponse(cashTransaction),
                transactionMapper.toBankResponse(bankTransaction)
        );
    }

    @Override
    @Transactional
    public TransactionDtos.TransactionGroupResponse withdrawBankToCash(TransactionDtos.BankWithdrawalRequest request) {
        Branch branch = findActiveBranch(request.branchId());
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId());
        String groupId = generateGroupId();

        ensureBankHasEnoughBalance(bankAccount, request.amount());

        BankTransaction bankTransaction = createBankTransaction(
                bankAccount,
                branch,
                BankTransactionType.CASH_WITHDRAWAL,
                CashDirection.OUT,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        PettyCashTransaction cashTransaction = createCashTransaction(
                branch,
                PettyCashTransactionType.BANK_WITHDRAWAL,
                CashDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        decreaseBankBalance(bankAccount, request.amount(), request.actionedBy());
        increaseBranchCash(branch, request.amount(), request.actionedBy());

        return new TransactionDtos.TransactionGroupResponse(
                groupId,
                transactionMapper.toCashResponse(cashTransaction),
                transactionMapper.toBankResponse(bankTransaction)
        );
    }

    @Override
    @Transactional
    public TransactionDtos.BranchTransferResponse transferCashBetweenBranches(TransactionDtos.BranchTransferRequest request) {
        if (request.fromBranchId().equals(request.toBranchId())) {
            throw new IllegalArgumentException("Cannot transfer cash to the same branch");
        }

        Branch fromBranch = findActiveBranch(request.fromBranchId());
        Branch toBranch = findActiveBranch(request.toBranchId());
        String groupId = generateGroupId();

        ensureBranchHasEnoughCash(fromBranch, request.amount());

        PettyCashTransaction transferOut = createCashTransaction(
                fromBranch,
                PettyCashTransactionType.BRANCH_TRANSFER_OUT,
                CashDirection.OUT,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        PettyCashTransaction transferIn = createCashTransaction(
                toBranch,
                PettyCashTransactionType.BRANCH_TRANSFER_IN,
                CashDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        decreaseBranchCash(fromBranch, request.amount(), request.actionedBy());
        increaseBranchCash(toBranch, request.amount(), request.actionedBy());

        return new TransactionDtos.BranchTransferResponse(
                groupId,
                transactionMapper.toCashResponse(transferOut),
                transactionMapper.toCashResponse(transferIn)
        );
    }

    @Override
    @Transactional
    public TransactionDtos.CashTransactionResponse adjustCashFloat(TransactionDtos.CashAdjustmentRequest request) {
        Branch branch = findActiveBranch(request.branchId());
        String groupId = generateGroupId();

        if (request.direction() == CashDirection.OUT) {
            ensureBranchHasEnoughCash(branch, request.amount());
        }

        PettyCashTransaction cashTransaction = createCashTransaction(
                branch,
                PettyCashTransactionType.ADJUSTMENT,
                request.direction(),
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        if (request.direction() == CashDirection.IN) {
            increaseBranchCash(branch, request.amount(), request.actionedBy());
        } else {
            decreaseBranchCash(branch, request.amount(), request.actionedBy());
        }

        return transactionMapper.toCashResponse(cashTransaction);
    }

    @Override
    @Transactional
    public TransactionDtos.BankTransactionResponse adjustBankBalance(TransactionDtos.BankAdjustmentRequest request) {
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId());
        Branch branch = resolveActiveBranch(request.branchId());
        String groupId = generateGroupId();

        if (request.direction() == CashDirection.OUT) {
            ensureBankHasEnoughBalance(bankAccount, request.amount());
        }

        BankTransaction bankTransaction = createBankTransaction(
                bankAccount,
                branch,
                BankTransactionType.ADJUSTMENT,
                request.direction(),
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        if (request.direction() == CashDirection.IN) {
            increaseBankBalance(bankAccount, request.amount(), request.actionedBy());
        } else {
            decreaseBankBalance(bankAccount, request.amount(), request.actionedBy());
        }

        return transactionMapper.toBankResponse(bankTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDtos.CashTransactionResponse> getAllCashTransactions() {
        return pettyCashTransactionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PettyCashTransaction::getId).reversed())
                .map(transactionMapper::toCashResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDtos.BankTransactionResponse> getAllBankTransactions() {
        return bankTransactionRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(BankTransaction::getId).reversed())
                .map(transactionMapper::toBankResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDtos.CashTransactionResponse> getCashTransactionsByBranch(Long branchId) {
        return pettyCashTransactionRepository.findByBranchId(branchId)
                .stream()
                .sorted(Comparator.comparing(PettyCashTransaction::getId).reversed())
                .map(transactionMapper::toCashResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionDtos.BankTransactionResponse> getBankTransactionsByAccount(Long bankAccountId) {
        return bankTransactionRepository.findByBankAccountId(bankAccountId)
                .stream()
                .sorted(Comparator.comparing(BankTransaction::getId).reversed())
                .map(transactionMapper::toBankResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDtos.TransactionGroupResponse getTransactionGroup(String transactionGroupId) {
        PettyCashTransaction cashTransaction = pettyCashTransactionRepository.findByTransactionGroupId(transactionGroupId)
                .stream()
                .findFirst()
                .orElse(null);

        BankTransaction bankTransaction = bankTransactionRepository.findByTransactionGroupId(transactionGroupId)
                .stream()
                .findFirst()
                .orElse(null);

        if (cashTransaction == null && bankTransaction == null) {
            throw new NotFoundException("Transaction group not found");
        }

        return new TransactionDtos.TransactionGroupResponse(
                transactionGroupId,
                cashTransaction == null ? null : transactionMapper.toCashResponse(cashTransaction),
                bankTransaction == null ? null : transactionMapper.toBankResponse(bankTransaction)
        );
    }

    private PettyCashTransaction createCashTransaction(
            Branch branch,
            PettyCashTransactionType transactionType,
            CashDirection direction,
            BigDecimal amount,
            java.time.LocalDate transactionDate,
            String transactionGroupId,
            String reference,
            String description,
            String actionedBy
    ) {
        PettyCashTransaction transaction = new PettyCashTransaction();
        transaction.setBranch(branch);
        transaction.setTransactionType(transactionType);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setTransactionDate(transactionDate);
        transaction.setTransactionGroupId(transactionGroupId);
        transaction.setStatus(TransactionStatus.POSTED);
        transaction.setReference(reference);
        transaction.setDescription(description);
        transaction.setActionedBy(actionedBy);
        return pettyCashTransactionRepository.save(transaction);
    }

    private BankTransaction createBankTransaction(
            BankAccount bankAccount,
            Branch branch,
            BankTransactionType transactionType,
            CashDirection direction,
            BigDecimal amount,
            java.time.LocalDate transactionDate,
            String transactionGroupId,
            String reference,
            String description,
            String actionedBy
    ) {
        BankTransaction transaction = new BankTransaction();
        transaction.setBankAccount(bankAccount);
        transaction.setBranch(branch);
        transaction.setTransactionType(transactionType);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setTransactionDate(transactionDate);
        transaction.setTransactionGroupId(transactionGroupId);
        transaction.setStatus(TransactionStatus.POSTED);
        transaction.setReference(reference);
        transaction.setDescription(description);
        transaction.setActionedBy(actionedBy);
        return bankTransactionRepository.save(transaction);
    }

    private Branch findActiveBranch(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found"));

        if (!branch.isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        return branch;
    }

    private Branch resolveActiveBranch(Long branchId) {
        if (branchId == null || branchId <= 0) {
            return null;
        }

        return findActiveBranch(branchId);
    }

    private BankAccount findActiveBankAccount(Long bankAccountId) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new NotFoundException("Bank account not found"));

        if (!bankAccount.isActive()) {
            throw new IllegalArgumentException("Bank account is inactive");
        }

        return bankAccount;
    }

    private void ensureBranchHasEnoughCash(Branch branch, BigDecimal amount) {
        if (branch.getCurrentCashFloatBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient branch cash float balance");
        }
    }

    private void ensureBankHasEnoughBalance(BankAccount bankAccount, BigDecimal amount) {
        if (bankAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient bank account balance");
        }
    }

    private void increaseBranchCash(Branch branch, BigDecimal amount, String actionedBy) {
        branch.setCurrentCashFloatBalance(branch.getCurrentCashFloatBalance().add(amount));
        branch.setActionedBy(actionedBy);
        branchRepository.save(branch);
    }

    private void decreaseBranchCash(Branch branch, BigDecimal amount, String actionedBy) {
        branch.setCurrentCashFloatBalance(branch.getCurrentCashFloatBalance().subtract(amount));
        branch.setActionedBy(actionedBy);
        branchRepository.save(branch);
    }

    private void increaseBankBalance(BankAccount bankAccount, BigDecimal amount, String actionedBy) {
        bankAccount.setCurrentBalance(bankAccount.getCurrentBalance().add(amount));
        bankAccount.setActionedBy(actionedBy);
        bankAccountRepository.save(bankAccount);
    }

    private void decreaseBankBalance(BankAccount bankAccount, BigDecimal amount, String actionedBy) {
        bankAccount.setCurrentBalance(bankAccount.getCurrentBalance().subtract(amount));
        bankAccount.setActionedBy(actionedBy);
        bankAccountRepository.save(bankAccount);
    }

    private String generateGroupId() {
        return "TXN-" + UUID.randomUUID();
    }
}