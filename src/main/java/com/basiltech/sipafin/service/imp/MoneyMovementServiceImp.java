package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.MoneyMovementDtos;
import com.basiltech.sipafin.mapper.CommissionMapper;
import com.basiltech.sipafin.mapper.MoneyMovementMapper;
import com.basiltech.sipafin.model.*;
import com.basiltech.sipafin.repository.*;
import com.basiltech.sipafin.service.MoneyMovementService;
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
public class MoneyMovementServiceImp implements MoneyMovementService {

    private final BranchRepository branchRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BranchFloatAccountRepository branchFloatAccountRepository;
    private final BranchFloatTransactionRepository branchFloatTransactionRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final MoneyMovementMapper moneyMovementMapper;
    private final CommissionMapper commissionMapper;
    private final BankCommissionAccountRepository bankCommissionAccountRepository;
    private final CommissionRepository commissionRepository;
    private final RevenueRepository revenueRepository;

    @Override
    @Transactional
    public MoneyMovementDtos.BranchFloatTransactionResponse openFloat(
            MoneyMovementDtos.CashOpeningBalanceRequest request
    ) {
        BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());
        String groupId = generateGroupId();

        BranchFloatTransaction transaction = postFloatTransaction(
                floatAccount,
                BranchFloatTransactionType.OPENING_BALANCE,
                MoneyDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return moneyMovementMapper.toFloatResponse(transaction);
    }

    @Override
    @Transactional
    public MoneyMovementDtos.MovementResponse depositFloatToBank(
            MoneyMovementDtos.BankDepositRequest request
    ) {
        BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId(), request.currency());
        String groupId = generateGroupId();

        ensureEnoughFloatBalance(floatAccount, request.amount());

        BranchFloatTransaction floatTransaction = postFloatTransaction(
                floatAccount,
                BranchFloatTransactionType.BANK_DEPOSIT,
                MoneyDirection.OUT,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        BankTransaction bankTransaction = postBankTransaction(
                bankAccount,
                floatAccount.getBranch(),
                BankLedgerTransactionType.CASH_DEPOSIT,
                MoneyDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        Commission commission = postCommissionIfConfigured(
                bankAccount,
                floatAccount.getBranch(),
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return new MoneyMovementDtos.MovementResponse(
                groupId,
                moneyMovementMapper.toFloatResponse(floatTransaction),
                moneyMovementMapper.toBankResponse(bankTransaction),
                commission == null ? null : commissionMapper.toCommissionResponse(commission)
        );
    }

    private Commission postCommissionIfConfigured(
            BankAccount bankAccount,
            Branch branch,
            BigDecimal baseAmount,
            LocalDate transactionDate,
            String groupId,
            String reference,
            String description,
            String actionedBy
    ) {
        BankCommissionAccount commissionAccount = bankCommissionAccountRepository
                .findByBankAccountIdAndActive(bankAccount.getId(), true)
                .orElse(null);

        if (commissionAccount == null) {
            return null;
        }

        BigDecimal commissionAmount = baseAmount
                .multiply(commissionAccount.getCommissionRate())
                .divide(BigDecimal.valueOf(100));

        if (commissionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        commissionAccount.setCurrentBalance(commissionAccount.getCurrentBalance().add(commissionAmount));
        commissionAccount.setActionedBy(actionedBy);
        bankCommissionAccountRepository.save(commissionAccount);

        Commission commission = new Commission();
        commission.setBranch(branch);
        commission.setBankAccount(bankAccount);
        commission.setCommissionAccount(commissionAccount);
        commission.setCurrency(bankAccount.getCurrency());
        commission.setBaseAmount(baseAmount);
        commission.setCommissionRate(commissionAccount.getCommissionRate());
        commission.setCommissionAmount(commissionAmount);
        commission.setCommissionDate(transactionDate);
        commission.setTransactionGroupId(groupId);
        commission.setStatus(CommissionStatus.RECEIVED);
        commission.setReference(reference);
        commission.setDescription(description == null ? "Auto commission on bank deposit" : description);
        commission.setActionedBy(actionedBy);

        return commissionRepository.save(commission);
    }

    @Override
    @Transactional
    public MoneyMovementDtos.MovementResponse withdrawBankToFloat(
            MoneyMovementDtos.BankWithdrawalRequest request
    ) {
        BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId(), request.currency());
        String groupId = generateGroupId();

        ensureEnoughBankBalance(bankAccount, request.amount());

        BankTransaction bankTransaction = postBankTransaction(
                bankAccount,
                floatAccount.getBranch(),
                BankLedgerTransactionType.CASH_WITHDRAWAL,
                MoneyDirection.OUT,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        BranchFloatTransaction floatTransaction = postFloatTransaction(
                floatAccount,
                BranchFloatTransactionType.BANK_WITHDRAWAL,
                MoneyDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return new MoneyMovementDtos.MovementResponse(
                groupId,
                moneyMovementMapper.toFloatResponse(floatTransaction),
                moneyMovementMapper.toBankResponse(bankTransaction),
                null
        );
    }

    @Override
    @Transactional
    public MoneyMovementDtos.BranchTransferResponse transferFloatBetweenBranches(
            MoneyMovementDtos.BranchTransferRequest request
    ) {
        if (request.fromBranchId().equals(request.toBranchId())) {
            throw new IllegalArgumentException("Cannot transfer to the same branch");
        }

        BranchFloatAccount fromFloat = findActiveFloatAccount(request.fromBranchId(), request.currency());
        BranchFloatAccount toFloat = findActiveFloatAccount(request.toBranchId(), request.currency());
        String groupId = generateGroupId();

        ensureEnoughFloatBalance(fromFloat, request.amount());

        BranchFloatTransaction outTransaction = postFloatTransaction(
                fromFloat,
                BranchFloatTransactionType.BRANCH_TRANSFER_OUT,
                MoneyDirection.OUT,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        BranchFloatTransaction inTransaction = postFloatTransaction(
                toFloat,
                BranchFloatTransactionType.BRANCH_TRANSFER_IN,
                MoneyDirection.IN,
                request.amount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return new MoneyMovementDtos.BranchTransferResponse(
                groupId,
                moneyMovementMapper.toFloatResponse(outTransaction),
                moneyMovementMapper.toFloatResponse(inTransaction)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MoneyMovementDtos.BranchCurrencyTransactionsResponse getTransactionsByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    ) {
        return new MoneyMovementDtos.BranchCurrencyTransactionsResponse(
                branchId,
                currency,
                getFloatTransactionsByBranchAndCurrency(branchId, currency),
                getBankTransactionsByBranchAndCurrency(branchId, currency)
        );
    }

    @Override
    @Transactional
    public MoneyMovementDtos.BranchFloatTransactionResponse adjustFloat(
            MoneyMovementDtos.FloatAdjustmentRequest request
    ) {
        BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());

        if (request.direction() == MoneyDirection.OUT) {
            ensureEnoughFloatBalance(floatAccount, request.amount());
        }

        BranchFloatTransaction transaction = postFloatTransaction(
                floatAccount,
                BranchFloatTransactionType.ADJUSTMENT,
                request.direction(),
                request.amount(),
                request.transactionDate(),
                generateGroupId(),
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return moneyMovementMapper.toFloatResponse(transaction);
    }

    @Override
    @Transactional
    public MoneyMovementDtos.BankTransactionResponse adjustBank(
            MoneyMovementDtos.BankAdjustmentRequest request
    ) {
        Branch branch = findActiveBranch(request.branchId());
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId(), request.currency());

        if (request.direction() == MoneyDirection.OUT) {
            ensureEnoughBankBalance(bankAccount, request.amount());
        }

        BankTransaction transaction = postBankTransaction(
                bankAccount,
                branch,
                BankLedgerTransactionType.ADJUSTMENT,
                request.direction(),
                request.amount(),
                request.transactionDate(),
                generateGroupId(),
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return moneyMovementMapper.toBankResponse(transaction);
    }

    @Override
    @Transactional
    public MoneyMovementDtos.CurrencyExchangeResponse exchangeCurrency(
            MoneyMovementDtos.CurrencyExchangeRequest request
    ) {
        if (request.receivedCurrency() == request.paidCurrency()) {
            throw new IllegalArgumentException("Received currency and paid currency cannot be the same");
        }

        BigDecimal paidAmount = request.paidAmount() == null
                ? request.receivedAmount().multiply(request.exchangeRate())
                : request.paidAmount();

        BranchFloatAccount receivedFloat = findActiveFloatAccount(request.branchId(), request.receivedCurrency());
        String groupId = generateGroupId();

        BranchFloatTransaction receivedTransaction = postFloatTransaction(
                receivedFloat,
                BranchFloatTransactionType.CURRENCY_EXCHANGE_IN,
                MoneyDirection.IN,
                request.receivedAmount(),
                request.transactionDate(),
                groupId,
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        BranchFloatTransaction paidFloatTransaction = null;
        BankTransaction paidBankTransaction = null;

        if (request.paidFrom() == MoneySourceType.BRANCH_FLOAT) {
            BranchFloatAccount paidFloat = findActiveFloatAccount(request.branchId(), request.paidCurrency());
            ensureEnoughFloatBalance(paidFloat, paidAmount);

            paidFloatTransaction = postFloatTransaction(
                    paidFloat,
                    BranchFloatTransactionType.CURRENCY_EXCHANGE_OUT,
                    MoneyDirection.OUT,
                    paidAmount,
                    request.transactionDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        } else {
            if (request.paidBankAccountId() == null) {
                throw new IllegalArgumentException("Paid bank account ID is required when paying from bank account");
            }

            BankAccount paidBankAccount = findActiveBankAccount(request.paidBankAccountId(), request.paidCurrency());
            ensureEnoughBankBalance(paidBankAccount, paidAmount);

            paidBankTransaction = postBankTransaction(
                    paidBankAccount,
                    receivedFloat.getBranch(),
                    BankLedgerTransactionType.CURRENCY_EXCHANGE_OUT,
                    MoneyDirection.OUT,
                    paidAmount,
                    request.transactionDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        }

        return new MoneyMovementDtos.CurrencyExchangeResponse(
                groupId,
                moneyMovementMapper.toFloatResponse(receivedTransaction),
                paidFloatTransaction == null ? null : moneyMovementMapper.toFloatResponse(paidFloatTransaction),
                paidBankTransaction == null ? null : moneyMovementMapper.toBankResponse(paidBankTransaction)
        );
    }

    @Override
    @Transactional
    public MoneyMovementDtos.BranchFloatTransactionResponse payExpenseFromFloat(
            MoneyMovementDtos.ExpensePaymentRequest request
    ) {
        if (request.paymentSource() != MoneySourceType.BRANCH_FLOAT) {
            throw new IllegalArgumentException("Payment source must be BRANCH_FLOAT");
        }

        BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());
        ensureEnoughFloatBalance(floatAccount, request.amount());

        BranchFloatTransaction transaction = postFloatTransaction(
                floatAccount,
                BranchFloatTransactionType.EXPENSE,
                MoneyDirection.OUT,
                request.amount(),
                request.transactionDate(),
                generateGroupId(),
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return moneyMovementMapper.toFloatResponse(transaction);
    }

    @Override
    @Transactional
    public MoneyMovementDtos.AccountTransferResponse transferBetweenAccounts(
            MoneyMovementDtos.AccountTransferRequest request
    ) {
        validateTransferRequest(request);

        String groupId = generateGroupId();

        BranchFloatTransaction sourceFloatTransaction = null;
        BankTransaction sourceBankTransaction = null;
        BranchFloatTransaction destinationFloatTransaction = null;
        BankTransaction destinationBankTransaction = null;

        Branch sourceBranch;

        if (request.fromType() == MoneySourceType.BRANCH_FLOAT) {
            BranchFloatAccount sourceFloat = findActiveFloatAccount(request.fromBranchId(), request.currency());
            ensureEnoughFloatBalance(sourceFloat, request.amount());
            sourceBranch = sourceFloat.getBranch();

            sourceFloatTransaction = postFloatTransaction(
                    sourceFloat,
                    BranchFloatTransactionType.TRANSFER_OUT,
                    MoneyDirection.OUT,
                    request.amount(),
                    request.transactionDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        } else {
            BankAccount sourceBankAccount = findActiveBankAccount(request.fromBankAccountId(), request.currency());
            ensureEnoughBankBalance(sourceBankAccount, request.amount());
            sourceBranch = resolveTransferBranch(request);

            sourceBankTransaction = postBankTransaction(
                    sourceBankAccount,
                    sourceBranch,
                    BankLedgerTransactionType.TRANSFER_OUT,
                    MoneyDirection.OUT,
                    request.amount(),
                    request.transactionDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        }

        if (request.toType() == MoneySourceType.BRANCH_FLOAT) {
            BranchFloatAccount destinationFloat = findActiveFloatAccount(request.toBranchId(), request.currency());

            destinationFloatTransaction = postFloatTransaction(
                    destinationFloat,
                    BranchFloatTransactionType.TRANSFER_IN,
                    MoneyDirection.IN,
                    request.amount(),
                    request.transactionDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        } else {
            BankAccount destinationBankAccount = findActiveBankAccount(request.toBankAccountId(), request.currency());

            destinationBankTransaction = postBankTransaction(
                    destinationBankAccount,
                    sourceBranch,
                    BankLedgerTransactionType.TRANSFER_IN,
                    MoneyDirection.IN,
                    request.amount(),
                    request.transactionDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        }

        return new MoneyMovementDtos.AccountTransferResponse(
                groupId,
                sourceFloatTransaction == null ? null : moneyMovementMapper.toFloatResponse(sourceFloatTransaction),
                sourceBankTransaction == null ? null : moneyMovementMapper.toBankResponse(sourceBankTransaction),
                destinationFloatTransaction == null ? null : moneyMovementMapper.toFloatResponse(destinationFloatTransaction),
                destinationBankTransaction == null ? null : moneyMovementMapper.toBankResponse(destinationBankTransaction)
        );
    }

    private void validateTransferRequest(MoneyMovementDtos.AccountTransferRequest request) {
        if (request.fromType() == request.toType()
                && request.fromType() == MoneySourceType.BRANCH_FLOAT
                && request.fromBranchId() != null
                && request.fromBranchId().equals(request.toBranchId())) {
            throw new IllegalArgumentException("Cannot transfer between the same branch float account");
        }

        if (request.fromType() == request.toType()
                && request.fromType() == MoneySourceType.BANK_ACCOUNT
                && request.fromBankAccountId() != null
                && request.fromBankAccountId().equals(request.toBankAccountId())) {
            throw new IllegalArgumentException("Cannot transfer between the same bank account");
        }

        if (request.fromType() == MoneySourceType.BRANCH_FLOAT && request.fromBranchId() == null) {
            throw new IllegalArgumentException("Source branch ID is required when source type is BRANCH_FLOAT");
        }

        if (request.fromType() == MoneySourceType.BANK_ACCOUNT && request.fromBankAccountId() == null) {
            throw new IllegalArgumentException("Source bank account ID is required when source type is BANK_ACCOUNT");
        }

        if (request.toType() == MoneySourceType.BRANCH_FLOAT && request.toBranchId() == null) {
            throw new IllegalArgumentException("Destination branch ID is required when destination type is BRANCH_FLOAT");
        }

        if (request.toType() == MoneySourceType.BANK_ACCOUNT && request.toBankAccountId() == null) {
            throw new IllegalArgumentException("Destination bank account ID is required when destination type is BANK_ACCOUNT");
        }
    }

    private Branch resolveTransferBranch(MoneyMovementDtos.AccountTransferRequest request) {
        if (request.toType() == MoneySourceType.BRANCH_FLOAT) {
            return findActiveBranch(request.toBranchId());
        }

        if (request.fromBranchId() != null) {
            return findActiveBranch(request.fromBranchId());
        }

        if (request.toBranchId() != null) {
            return findActiveBranch(request.toBranchId());
        }

        throw new IllegalArgumentException("Branch ID is required for bank-to-bank transfers");
    }

    @Override
    @Transactional
    public MoneyMovementDtos.BankTransactionResponse payExpenseFromBank(
            MoneyMovementDtos.ExpensePaymentRequest request
    ) {
        if (request.paymentSource() != MoneySourceType.BANK_ACCOUNT) {
            throw new IllegalArgumentException("Payment source must be BANK_ACCOUNT");
        }

        if (request.bankAccountId() == null) {
            throw new IllegalArgumentException("Bank account ID is required when paying from bank account");
        }

        Branch branch = findActiveBranch(request.branchId());
        BankAccount bankAccount = findActiveBankAccount(request.bankAccountId(), request.currency());
        ensureEnoughBankBalance(bankAccount, request.amount());

        BankTransaction transaction = postBankTransaction(
                bankAccount,
                branch,
                BankLedgerTransactionType.EXPENSE_PAYMENT,
                MoneyDirection.OUT,
                request.amount(),
                request.transactionDate(),
                generateGroupId(),
                request.reference(),
                request.description(),
                request.actionedBy()
        );

        return moneyMovementMapper.toBankResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BranchFloatTransactionResponse> getFloatTransactionsByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    ) {
        return branchFloatTransactionRepository.findByBranchIdAndCurrency(branchId, currency)
                .stream()
                .sorted(Comparator.comparing(BranchFloatTransaction::getId).reversed())
                .map(moneyMovementMapper::toFloatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BranchFloatTransactionResponse> getFloatTransactionsByBranchCurrencyAndDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate
    ) {
        return branchFloatTransactionRepository
                .findByBranchIdAndCurrencyAndTransactionDate(branchId, currency, transactionDate)
                .stream()
                .sorted(Comparator.comparing(BranchFloatTransaction::getId).reversed())
                .map(moneyMovementMapper::toFloatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanReceivedFloatMovements(
            Long branchId,
            CurrencyCode currency
    ) {
        return branchFloatTransactionRepository
                .findByBranchIdAndCurrencyAndTransactionType(
                        branchId,
                        currency,
                        BranchFloatTransactionType.LOAN_RECEIVED
                )
                .stream()
                .sorted(Comparator.comparing(BranchFloatTransaction::getId).reversed())
                .map(moneyMovementMapper::toFloatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanRepaymentFloatMovements(
            Long branchId,
            CurrencyCode currency
    ) {
        return branchFloatTransactionRepository
                .findByBranchIdAndCurrencyAndTransactionType(
                        branchId,
                        currency,
                        BranchFloatTransactionType.LOAN_REPAYMENT
                )
                .stream()
                .sorted(Comparator.comparing(BranchFloatTransaction::getId).reversed())
                .map(moneyMovementMapper::toFloatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanReceivedFloatMovementsByDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate
    ) {
        return branchFloatTransactionRepository
                .findByBranchIdAndCurrencyAndTransactionDateAndTransactionType(
                        branchId,
                        currency,
                        transactionDate,
                        BranchFloatTransactionType.LOAN_RECEIVED
                )
                .stream()
                .sorted(Comparator.comparing(BranchFloatTransaction::getId).reversed())
                .map(moneyMovementMapper::toFloatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BranchFloatTransactionResponse> getLoanRepaymentFloatMovementsByDate(
            Long branchId,
            CurrencyCode currency,
            LocalDate transactionDate
    ) {
        return branchFloatTransactionRepository
                .findByBranchIdAndCurrencyAndTransactionDateAndTransactionType(
                        branchId,
                        currency,
                        transactionDate,
                        BranchFloatTransactionType.LOAN_REPAYMENT
                )
                .stream()
                .sorted(Comparator.comparing(BranchFloatTransaction::getId).reversed())
                .map(moneyMovementMapper::toFloatResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BankTransactionResponse> getBankTransactionsByBranchAndCurrency(
            Long branchId,
            CurrencyCode currency
    ) {
        return bankTransactionRepository.findByBranchIdAndCurrency(branchId, currency)
                .stream()
                .sorted(Comparator.comparing(BankTransaction::getId).reversed())
                .map(moneyMovementMapper::toBankResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoneyMovementDtos.BankTransactionResponse> getBankTransactionsByAccountAndCurrency(
            Long bankAccountId,
            CurrencyCode currency
    ) {
        return bankTransactionRepository.findByBankAccountIdAndCurrency(bankAccountId, currency)
                .stream()
                .sorted(Comparator.comparing(BankTransaction::getId).reversed())
                .map(moneyMovementMapper::toBankResponse)
                .toList();
    }

    private BranchFloatTransaction postFloatTransaction(
            BranchFloatAccount floatAccount,
            BranchFloatTransactionType type,
            MoneyDirection direction,
            BigDecimal amount,
            LocalDate transactionDate,
            String groupId,
            String reference,
            String description,
            String actionedBy
    ) {
        BigDecimal balanceAfter = direction == MoneyDirection.IN
                ? floatAccount.getCurrentBalance().add(amount)
                : floatAccount.getCurrentBalance().subtract(amount);

        floatAccount.setCurrentBalance(balanceAfter);
        floatAccount.setActionedBy(actionedBy);
        branchFloatAccountRepository.save(floatAccount);

        BranchFloatTransaction transaction = new BranchFloatTransaction();
        transaction.setBranchFloatAccount(floatAccount);
        transaction.setBranch(floatAccount.getBranch());
        transaction.setCurrency(floatAccount.getCurrency());
        transaction.setTransactionType(type);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionDate(transactionDate);
        transaction.setTransactionGroupId(groupId);
        transaction.setStatus(TransactionStatus.POSTED);
        transaction.setReference(reference);
        transaction.setDescription(description);
        transaction.setActionedBy(actionedBy);

        return branchFloatTransactionRepository.save(transaction);
    }

    private BankTransaction postBankTransaction(
            BankAccount bankAccount,
            Branch branch,
            BankLedgerTransactionType type,
            MoneyDirection direction,
            BigDecimal amount,
            LocalDate transactionDate,
            String groupId,
            String reference,
            String description,
            String actionedBy
    ) {
        BigDecimal balanceAfter = direction == MoneyDirection.IN
                ? bankAccount.getCurrentBalance().add(amount)
                : bankAccount.getCurrentBalance().subtract(amount);

        bankAccount.setCurrentBalance(balanceAfter);
        bankAccount.setActionedBy(actionedBy);
        bankAccountRepository.save(bankAccount);

        BankTransaction transaction = new BankTransaction();
        transaction.setBankAccount(bankAccount);
        transaction.setBranch(branch);
        transaction.setCurrency(bankAccount.getCurrency());
        transaction.setTransactionType(type);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionDate(transactionDate);
        transaction.setTransactionGroupId(groupId);
        transaction.setStatus(TransactionStatus.POSTED);
        transaction.setReference(reference);
        transaction.setDescription(description);
        transaction.setActionedBy(actionedBy);

        return bankTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public MoneyMovementDtos.RevenueResponse receiveRevenue(
            MoneyMovementDtos.RevenueReceiptRequest request
    ) {
        Branch branch = findActiveBranch(request.branchId());
        String groupId = generateGroupId();

        BankAccount bankAccount = null;

        if (request.source() == MoneySourceType.BRANCH_FLOAT) {
            BranchFloatAccount floatAccount = findActiveFloatAccount(request.branchId(), request.currency());

            postFloatTransaction(
                    floatAccount,
                    BranchFloatTransactionType.REVENUE_RECEIPT,
                    MoneyDirection.IN,
                    request.amount(),
                    request.revenueDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        } else {
            if (request.bankAccountId() == null) {
                throw new IllegalArgumentException("Bank account ID is required when revenue source is BANK_ACCOUNT");
            }

            bankAccount = findActiveBankAccount(request.bankAccountId(), request.currency());

            postBankTransaction(
                    bankAccount,
                    branch,
                    BankLedgerTransactionType.REVENUE_RECEIPT,
                    MoneyDirection.IN,
                    request.amount(),
                    request.revenueDate(),
                    groupId,
                    request.reference(),
                    request.description(),
                    request.actionedBy()
            );
        }

        Revenue revenue = new Revenue();
        revenue.setBranch(branch);
        revenue.setBankAccount(bankAccount);
        revenue.setCurrency(request.currency());
        revenue.setCategory(request.category());
        revenue.setSource(request.source());
        revenue.setAmount(request.amount());
        revenue.setRevenueDate(request.revenueDate());
        revenue.setTransactionGroupId(groupId);
        revenue.setStatus(RevenueStatus.POSTED);
        revenue.setReference(request.reference());
        revenue.setDescription(request.description());
        revenue.setActionedBy(request.actionedBy());

        return toRevenueResponse(revenueRepository.save(revenue));
    }

    private MoneyMovementDtos.RevenueResponse toRevenueResponse(Revenue revenue) {
        BankAccount bankAccount = revenue.getBankAccount();

        return new MoneyMovementDtos.RevenueResponse(
                revenue.getId(),
                revenue.getBranch().getId(),
                revenue.getBranch().getName(),
                bankAccount == null ? null : bankAccount.getId(),
                bankAccount == null ? null : bankAccount.getBank().getName(),
                bankAccount == null ? null : bankAccount.getAccountName(),
                revenue.getCurrency(),
                revenue.getCategory(),
                revenue.getSource(),
                revenue.getAmount(),
                revenue.getRevenueDate(),
                revenue.getTransactionGroupId(),
                revenue.getStatus(),
                revenue.getReference(),
                revenue.getDescription(),
                revenue.getActionedBy(),
                revenue.getCreatedAt(),
                revenue.getUpdatedAt()
        );
    }

    private BranchFloatAccount findActiveFloatAccount(Long branchId, CurrencyCode currency) {
        BranchFloatAccount floatAccount = branchFloatAccountRepository.findByBranchIdAndCurrency(branchId, currency)
                .orElseThrow(() -> new NotFoundException("Branch float account not found for selected branch and currency"));

        if (!floatAccount.isActive()) {
            throw new IllegalArgumentException("Branch float account is inactive");
        }

        if (!floatAccount.getBranch().isActive()) {
            throw new IllegalArgumentException("Branch is inactive");
        }

        return floatAccount;
    }

    private BankAccount findActiveBankAccount(Long bankAccountId, CurrencyCode currency) {
        BankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new NotFoundException("Bank account not found"));

        if (!bankAccount.isActive()) {
            throw new IllegalArgumentException("Bank account is inactive");
        }

        if (!bankAccount.getCurrency().equals(currency)) {
            throw new IllegalArgumentException("Selected bank account currency does not match transaction currency");
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

//    @Override
//    @Transactional(readOnly = true)
//    public List<MoneyMovementDtos.BankTransactionResponse> getBankTransactionsByAccountAndCurrency(
//            Long bankAccountId,
//            CurrencyCode currency
//    ) {
//        return bankTransactionRepository.findByBankAccountIdAndCurrency(bankAccountId, currency)
//                .stream()
//                .sorted(Comparator.comparing(BankTransaction::getId).reversed())
//                .map(moneyMovementMapper::toBankResponse)
//                .toList();
//    }

    @Override
    @Transactional(readOnly = true)
    public MoneyMovementDtos.BranchFloatTransactionResponse getFloatTransactionById(Long id) {
        return moneyMovementMapper.toFloatResponse(findFloatTransaction(id));
    }

    @Override
    @Transactional
    public MoneyMovementDtos.BranchFloatTransactionResponse updateFloatTransaction(
            Long id,
            MoneyMovementDtos.UpdateBranchFloatTransactionRequest request
    ) {
        BranchFloatTransaction transaction = findFloatTransaction(id);
        BranchFloatAccount floatAccount = transaction.getBranchFloatAccount();

        BigDecimal currentBalance = floatAccount.getCurrentBalance();
        BigDecimal balanceWithoutOldTransaction = reverseTransactionEffect(
                currentBalance,
                transaction.getDirection(),
                transaction.getAmount()
        );

        BigDecimal newBalance = applyTransactionEffect(
                balanceWithoutOldTransaction,
                request.direction(),
                request.amount()
        );

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Updating this transaction would make branch float balance negative");
        }

        floatAccount.setCurrentBalance(newBalance);
        floatAccount.setActionedBy(request.actionedBy());
        branchFloatAccountRepository.save(floatAccount);

        transaction.setDirection(request.direction());
        transaction.setAmount(request.amount());
        transaction.setBalanceAfter(newBalance);
        transaction.setTransactionDate(request.transactionDate());
        transaction.setStatus(request.status());
        transaction.setReference(request.reference());
        transaction.setDescription(request.description());
        transaction.setActionedBy(request.actionedBy());

        return moneyMovementMapper.toFloatResponse(branchFloatTransactionRepository.save(transaction));
    }


    @Override
    @Transactional(readOnly = true)
    public MoneyMovementDtos.BankTransactionResponse getBankTransactionById(Long id) {
        return moneyMovementMapper.toBankResponse(findBankTransaction(id));
    }

    @Override
    @Transactional
    public MoneyMovementDtos.BankTransactionResponse updateBankTransaction(
            Long id,
            MoneyMovementDtos.UpdateBankTransactionRequest request
    ) {
        BankTransaction transaction = findBankTransaction(id);
        BankAccount bankAccount = transaction.getBankAccount();

        BigDecimal currentBalance = bankAccount.getCurrentBalance();
        BigDecimal balanceWithoutOldTransaction = reverseTransactionEffect(
                currentBalance,
                transaction.getDirection(),
                transaction.getAmount()
        );

        BigDecimal newBalance = applyTransactionEffect(
                balanceWithoutOldTransaction,
                request.direction(),
                request.amount()
        );

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Updating this transaction would make bank account balance negative");
        }

        bankAccount.setCurrentBalance(newBalance);
        bankAccount.setActionedBy(request.actionedBy());
        bankAccountRepository.save(bankAccount);

        transaction.setDirection(request.direction());
        transaction.setAmount(request.amount());
        transaction.setBalanceAfter(newBalance);
        transaction.setTransactionDate(request.transactionDate());
        transaction.setStatus(request.status());
        transaction.setReference(request.reference());
        transaction.setDescription(request.description());
        transaction.setActionedBy(request.actionedBy());

        return moneyMovementMapper.toBankResponse(bankTransactionRepository.save(transaction));
    }

    private BigDecimal reverseTransactionEffect(
            BigDecimal currentBalance,
            MoneyDirection oldDirection,
            BigDecimal oldAmount
    ) {
        if (oldDirection == MoneyDirection.IN) {
            return currentBalance.subtract(oldAmount);
        }

        return currentBalance.add(oldAmount);
    }

    private BigDecimal applyTransactionEffect(
            BigDecimal balanceBefore,
            MoneyDirection newDirection,
            BigDecimal newAmount
    ) {
        if (newDirection == MoneyDirection.IN) {
            return balanceBefore.add(newAmount);
        }

        return balanceBefore.subtract(newAmount);
    }

    private BranchFloatTransaction findFloatTransaction(Long id) {
        return branchFloatTransactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch float transaction not found"));
    }

    private BankTransaction findBankTransaction(Long id) {
        return bankTransactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bank transaction not found"));
    }

    private void ensureEnoughFloatBalance(BranchFloatAccount floatAccount, BigDecimal amount) {
        if (floatAccount.getCurrentBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient branch float balance");
        }
    }

    private void ensureEnoughBankBalance(BankAccount bankAccount, BigDecimal amount) {
        if (bankAccount.getCurrentBalance().compareTo(amount) < 0) {
//            throw new IllegalArgumentException("Insufficient bank account balance");

        }
    }

    private String generateGroupId() {
        return "TXN-" + UUID.randomUUID();
    }
}