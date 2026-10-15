package com.basiltech.sipafin.service.imp;

import com.basiltech.sipafin.dto.DashboardDtos;
import com.basiltech.sipafin.model.*;
import com.basiltech.sipafin.repository.*;
import com.basiltech.sipafin.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImp implements DashboardService {

    private final BranchFloatAccountRepository branchFloatAccountRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BranchFloatTransactionRepository branchFloatTransactionRepository;
    private final BankTransactionRepository bankTransactionRepository;
    private final LoanAccountRepository loanAccountRepository;
    private final ClientLoanAccountRepository clientLoanAccountRepository;
    private final BankReconciliationRepository bankReconciliationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDtos.DashboardResponse getDashboard(LocalDate fromDate, LocalDate toDate) {
        LocalDate resolvedToDate = toDate == null ? LocalDate.now() : toDate;
        LocalDate resolvedFromDate = fromDate == null ? resolvedToDate : fromDate;

        if (resolvedFromDate.isAfter(resolvedToDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }

        List<BranchFloatAccount> floatAccounts = branchFloatAccountRepository.findAll();
        List<BankAccount> bankAccounts = bankAccountRepository.findAll();

        List<BranchFloatTransaction> floatTransactions = branchFloatTransactionRepository
                .findByTransactionDateBetween(resolvedFromDate, resolvedToDate);

        List<BankTransaction> bankTransactions = bankTransactionRepository
                .findByTransactionDateBetween(resolvedFromDate, resolvedToDate);

        List<BankReconciliation> reconciliations = bankReconciliationRepository
                .findByReconciliationDateBetween(resolvedFromDate, resolvedToDate);

        List<LoanAccount> investorLoans = loanAccountRepository.findAll();
        List<ClientLoanAccount> clientLoans = clientLoanAccountRepository.findAll();

        List<DashboardDtos.CurrencyBalanceSummary> floatBalances = summarizeFloatBalances(floatAccounts);
        List<DashboardDtos.CurrencyBalanceSummary> bankBalances = summarizeBankBalances(bankAccounts);

        return new DashboardDtos.DashboardResponse(
                resolvedFromDate,
                resolvedToDate,
                floatBalances,
                bankBalances,
                summarizeTotalLiquidity(floatBalances, bankBalances),
                summarizeInvestorLoans(investorLoans),
                summarizeClientLoans(clientLoans),
                summarizeActivity(floatTransactions, bankTransactions),
                summarizeReconciliation(reconciliations, bankAccounts)
        );
    }

    private List<DashboardDtos.CurrencyBalanceSummary> summarizeFloatBalances(List<BranchFloatAccount> accounts) {
        return accounts.stream()
                .filter(BranchFloatAccount::isActive)
                .collect(Collectors.groupingBy(
                        BranchFloatAccount::getCurrency,
                        () -> new EnumMap<>(CurrencyCode.class),
                        Collectors.reducing(BigDecimal.ZERO, BranchFloatAccount::getCurrentBalance, BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .map(entry -> new DashboardDtos.CurrencyBalanceSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(summary -> summary.currency().name()))
                .toList();
    }

    private List<DashboardDtos.CurrencyBalanceSummary> summarizeBankBalances(List<BankAccount> accounts) {
        return accounts.stream()
                .filter(BankAccount::isActive)
                .collect(Collectors.groupingBy(
                        BankAccount::getCurrency,
                        () -> new EnumMap<>(CurrencyCode.class),
                        Collectors.reducing(BigDecimal.ZERO, BankAccount::getCurrentBalance, BigDecimal::add)
                ))
                .entrySet()
                .stream()
                .map(entry -> new DashboardDtos.CurrencyBalanceSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(summary -> summary.currency().name()))
                .toList();
    }

    private List<DashboardDtos.CurrencyBalanceSummary> summarizeTotalLiquidity(
            List<DashboardDtos.CurrencyBalanceSummary> floatBalances,
            List<DashboardDtos.CurrencyBalanceSummary> bankBalances
    ) {
        Map<CurrencyCode, BigDecimal> totals = new EnumMap<>(CurrencyCode.class);

        floatBalances.forEach(summary ->
                totals.merge(summary.currency(), summary.amount(), BigDecimal::add)
        );

        bankBalances.forEach(summary ->
                totals.merge(summary.currency(), summary.amount(), BigDecimal::add)
        );

        return totals.entrySet()
                .stream()
                .map(entry -> new DashboardDtos.CurrencyBalanceSummary(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(summary -> summary.currency().name()))
                .toList();
    }

    private DashboardDtos.LoanDashboardSummary summarizeInvestorLoans(List<LoanAccount> loans) {
        return new DashboardDtos.LoanDashboardSummary(
                loans.size(),
                countInvestorLoansByStatus(loans, LoanStatus.ACTIVE),
                countInvestorLoansByStatus(loans, LoanStatus.PARTIALLY_PAID),
                countInvestorLoansByStatus(loans, LoanStatus.PAID),
                countInvestorLoansByStatus(loans, LoanStatus.CANCELLED),
                sumInvestorLoans(loans, LoanAccount::getPrincipalAmount),
                sumInvestorLoans(loans, LoanAccount::getTotalPayable),
                sumInvestorLoans(loans, LoanAccount::getOutstandingBalance)
        );
    }

    private DashboardDtos.ClientLoanDashboardSummary summarizeClientLoans(List<ClientLoanAccount> loans) {
        return new DashboardDtos.ClientLoanDashboardSummary(
                loans.size(),
                countClientLoansByStatus(loans, ClientLoanStatus.ACTIVE),
                countClientLoansByStatus(loans, ClientLoanStatus.PARTIALLY_PAID),
                countClientLoansByStatus(loans, ClientLoanStatus.PAID),
                countClientLoansByStatus(loans, ClientLoanStatus.CANCELLED),
                sumClientLoans(loans, ClientLoanAccount::getPrincipalAmount),
                sumClientLoans(loans, ClientLoanAccount::getTotalReceivable),
                sumClientLoans(loans, ClientLoanAccount::getOutstandingBalance)
        );
    }

    private DashboardDtos.ActivityDashboardSummary summarizeActivity(
            List<BranchFloatTransaction> floatTransactions,
            List<BankTransaction> bankTransactions
    ) {
        return new DashboardDtos.ActivityDashboardSummary(
                sumFloatTransactions(floatTransactions, MoneyDirection.IN),
                sumFloatTransactions(floatTransactions, MoneyDirection.OUT),
                sumBankTransactions(bankTransactions, MoneyDirection.IN),
                sumBankTransactions(bankTransactions, MoneyDirection.OUT),
                sumFloatTransactionsByType(floatTransactions, BranchFloatTransactionType.BANK_DEPOSIT),
                sumFloatTransactionsByType(floatTransactions, BranchFloatTransactionType.BANK_WITHDRAWAL),
                sumFloatTransactionsByType(floatTransactions, BranchFloatTransactionType.EXPENSE),
                sumBankTransactionsByType(bankTransactions, BankLedgerTransactionType.EXPENSE_PAYMENT),
                sumFloatTransactionsByType(floatTransactions, BranchFloatTransactionType.LOAN_RECEIVED),
                sumFloatTransactionsByType(floatTransactions, BranchFloatTransactionType.LOAN_REPAYMENT),
                sumFloatTransactionsByType(floatTransactions, BranchFloatTransactionType.CLIENT_LOAN_ISSUED)
                        .add(sumBankTransactionsByType(bankTransactions, BankLedgerTransactionType.CLIENT_LOAN_ISSUED)),
                sumFloatTransactionsByType(floatTransactions, BranchFloatTransactionType.CLIENT_LOAN_REPAYMENT)
                        .add(sumBankTransactionsByType(bankTransactions, BankLedgerTransactionType.CLIENT_LOAN_REPAYMENT)),
                sumBankTransactionsByType(bankTransactions, BankLedgerTransactionType.RECONCILIATION_ADJUSTMENT)
        );
    }

    private DashboardDtos.ReconciliationDashboardSummary summarizeReconciliation(
            List<BankReconciliation> reconciliations,
            List<BankAccount> bankAccounts
    ) {
        BigDecimal totalDifference = reconciliations.stream()
                .map(BankReconciliation::getDifference)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalShortAmount = reconciliations.stream()
                .filter(reconciliation -> reconciliation.getDifferenceType() == ReconciliationDifferenceType.SHORT)
                .map(reconciliation -> reconciliation.getDifference().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExcessAmount = reconciliations.stream()
                .filter(reconciliation -> reconciliation.getDifferenceType() == ReconciliationDifferenceType.EXCESS)
                .map(BankReconciliation::getDifference)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReconciledAmount = reconciliations.stream()
                .map(BankReconciliation::getReconciledAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalUnreconciledAmount = reconciliations.stream()
                .map(BankReconciliation::getUnreconciledAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardDtos.ReconciliationDashboardSummary(
                reconciliations.size(),
                countReconciliationsByStatus(reconciliations, ReconciliationStatus.DRAFT),
                countReconciliationsByStatus(reconciliations, ReconciliationStatus.BALANCED),
                countReconciliationsByStatus(reconciliations, ReconciliationStatus.PARTIALLY_RECONCILED),
                countReconciliationsByStatus(reconciliations, ReconciliationStatus.UNRESOLVED),
                countReconciliationsByStatus(reconciliations, ReconciliationStatus.CANCELLED),
                totalDifference,
                totalShortAmount,
                totalExcessAmount,
                totalReconciledAmount,
                totalUnreconciledAmount,
                buildStatusCounts(reconciliations),
                buildUnresolvedItems(reconciliations),
                buildBankAccountHealth(bankAccounts),
                buildRecentReconciliations(reconciliations)
        );
    }

    private List<DashboardDtos.ReconciliationStatusCount> buildStatusCounts(List<BankReconciliation> reconciliations) {
        return List.of(
                new DashboardDtos.ReconciliationStatusCount(
                        ReconciliationStatus.DRAFT,
                        countReconciliationsByStatus(reconciliations, ReconciliationStatus.DRAFT)
                ),
                new DashboardDtos.ReconciliationStatusCount(
                        ReconciliationStatus.BALANCED,
                        countReconciliationsByStatus(reconciliations, ReconciliationStatus.BALANCED)
                ),
                new DashboardDtos.ReconciliationStatusCount(
                        ReconciliationStatus.PARTIALLY_RECONCILED,
                        countReconciliationsByStatus(reconciliations, ReconciliationStatus.PARTIALLY_RECONCILED)
                ),
                new DashboardDtos.ReconciliationStatusCount(
                        ReconciliationStatus.UNRESOLVED,
                        countReconciliationsByStatus(reconciliations, ReconciliationStatus.UNRESOLVED)
                ),
                new DashboardDtos.ReconciliationStatusCount(
                        ReconciliationStatus.CANCELLED,
                        countReconciliationsByStatus(reconciliations, ReconciliationStatus.CANCELLED)
                )
        );
    }

    private List<DashboardDtos.UnresolvedReconciliationInsight> buildUnresolvedItems(
            List<BankReconciliation> reconciliations
    ) {
        return reconciliations.stream()
                .filter(reconciliation ->
                        reconciliation.getStatus() == ReconciliationStatus.UNRESOLVED
                                || reconciliation.getStatus() == ReconciliationStatus.PARTIALLY_RECONCILED
                )
                .sorted(Comparator.comparing(BankReconciliation::getReconciliationDate).reversed())
                .limit(20)
                .map(reconciliation -> {
                    Branch branch = reconciliation.getBranch();

                    return new DashboardDtos.UnresolvedReconciliationInsight(
                            reconciliation.getId(),
                            reconciliation.getBankAccount().getId(),
                            reconciliation.getBankAccount().getBank().getName(),
                            reconciliation.getBankAccount().getAccountName(),
                            reconciliation.getBankAccount().getAccountNumber(),
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
                            reconciliation.getReference(),
                            reconciliation.getReason(),
                            reconciliation.getActionedBy()
                    );
                })
                .toList();
    }

    private List<DashboardDtos.BankReconciliationHealth> buildBankAccountHealth(List<BankAccount> bankAccounts) {
        LocalDate today = LocalDate.now();

        return bankAccounts.stream()
                .filter(BankAccount::isActive)
                .map(bankAccount -> {
                    BankReconciliation latest = bankReconciliationRepository.findByBankAccountId(bankAccount.getId())
                            .stream()
                            .max(Comparator.comparing(BankReconciliation::getReconciliationDate)
                                    .thenComparing(BankReconciliation::getId))
                            .orElse(null);

                    LocalDate lastDate = latest == null ? null : latest.getReconciliationDate();
                    long daysSinceLast = lastDate == null ? -1 : ChronoUnit.DAYS.between(lastDate, today);

                    return new DashboardDtos.BankReconciliationHealth(
                            bankAccount.getId(),
                            bankAccount.getBank().getName(),
                            bankAccount.getAccountName(),
                            bankAccount.getAccountNumber(),
                            bankAccount.getCurrency(),
                            bankAccount.getCurrentBalance(),
                            lastDate,
                            latest == null ? null : latest.getStatus(),
                            latest == null ? null : latest.getDifferenceType(),
                            latest == null ? BigDecimal.ZERO : latest.getDifference(),
                            latest == null ? BigDecimal.ZERO : latest.getUnreconciledAmount(),
                            daysSinceLast,
                            resolveHealthStatus(latest, daysSinceLast)
                    );
                })
                .sorted(Comparator
                        .comparing((DashboardDtos.BankReconciliationHealth item) -> healthPriority(item.healthStatus()))
                        .thenComparing(DashboardDtos.BankReconciliationHealth::bankName)
                        .thenComparing(DashboardDtos.BankReconciliationHealth::accountName))
                .toList();
    }

    private String resolveHealthStatus(BankReconciliation latest, long daysSinceLast) {
        if (latest == null) {
            return "NEVER_RECONCILED";
        }

        if (latest.getStatus() == ReconciliationStatus.UNRESOLVED) {
            return "UNRESOLVED";
        }

        if (latest.getStatus() == ReconciliationStatus.PARTIALLY_RECONCILED) {
            return "PARTIALLY_RECONCILED";
        }

        if (latest.getStatus() == ReconciliationStatus.CANCELLED) {
            return "CANCELLED";
        }

        if (daysSinceLast > 30) {
            return "STALE";
        }

        return "HEALTHY";
    }

    private int healthPriority(String healthStatus) {
        return switch (healthStatus) {
            case "UNRESOLVED" -> 1;
            case "PARTIALLY_RECONCILED" -> 2;
            case "NEVER_RECONCILED" -> 3;
            case "STALE" -> 4;
            case "CANCELLED" -> 5;
            case "HEALTHY" -> 6;
            default -> 9;
        };
    }

    private List<DashboardDtos.RecentReconciliation> buildRecentReconciliations(List<BankReconciliation> reconciliations) {
        return reconciliations.stream()
                .sorted(Comparator.comparing(BankReconciliation::getReconciliationDate)
                        .thenComparing(BankReconciliation::getId)
                        .reversed())
                .limit(20)
                .map(reconciliation -> {
                    Branch branch = reconciliation.getBranch();

                    return new DashboardDtos.RecentReconciliation(
                            reconciliation.getId(),
                            reconciliation.getBankAccount().getId(),
                            reconciliation.getBankAccount().getBank().getName(),
                            reconciliation.getBankAccount().getAccountName(),
                            reconciliation.getBankAccount().getAccountNumber(),
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
                            reconciliation.getStatus(),
                            reconciliation.getReference(),
                            reconciliation.getReason(),
                            reconciliation.getActionedBy()
                    );
                })
                .toList();
    }

    private long countInvestorLoansByStatus(List<LoanAccount> loans, LoanStatus status) {
        return loans.stream()
                .filter(loan -> loan.getStatus() == status)
                .count();
    }

    private long countClientLoansByStatus(List<ClientLoanAccount> loans, ClientLoanStatus status) {
        return loans.stream()
                .filter(loan -> loan.getStatus() == status)
                .count();
    }

    private long countReconciliationsByStatus(List<BankReconciliation> reconciliations, ReconciliationStatus status) {
        return reconciliations.stream()
                .filter(reconciliation -> reconciliation.getStatus() == status)
                .count();
    }

    private BigDecimal sumFloatTransactions(List<BranchFloatTransaction> transactions, MoneyDirection direction) {
        return transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.POSTED)
                .filter(transaction -> transaction.getDirection() == direction)
                .map(BranchFloatTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBankTransactions(List<BankTransaction> transactions, MoneyDirection direction) {
        return transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.POSTED)
                .filter(transaction -> transaction.getDirection() == direction)
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumFloatTransactionsByType(
            List<BranchFloatTransaction> transactions,
            BranchFloatTransactionType type
    ) {
        return transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.POSTED)
                .filter(transaction -> transaction.getTransactionType() == type)
                .map(BranchFloatTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumBankTransactionsByType(
            List<BankTransaction> transactions,
            BankLedgerTransactionType type
    ) {
        return transactions.stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.POSTED)
                .filter(transaction -> transaction.getTransactionType() == type)
                .map(BankTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumInvestorLoans(
            List<LoanAccount> loans,
            Function<LoanAccount, BigDecimal> mapper
    ) {
        return loans.stream()
                .filter(loan -> loan.getStatus() != LoanStatus.CANCELLED)
                .map(mapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumClientLoans(
            List<ClientLoanAccount> loans,
            Function<ClientLoanAccount, BigDecimal> mapper
    ) {
        return loans.stream()
                .filter(loan -> loan.getStatus() != ClientLoanStatus.CANCELLED)
                .map(mapper)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}