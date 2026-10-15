package com.basiltech.sipafin.dto;

import com.basiltech.sipafin.model.CurrencyCode;
import com.basiltech.sipafin.model.ReconciliationDifferenceType;
import com.basiltech.sipafin.model.ReconciliationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardDtos {

    public record DashboardResponse(
            LocalDate fromDate,
            LocalDate toDate,
            List<CurrencyBalanceSummary> floatBalances,
            List<CurrencyBalanceSummary> bankBalances,
            List<CurrencyBalanceSummary> totalLiquidity,
            LoanDashboardSummary investorLoans,
            ClientLoanDashboardSummary clientLoans,
            ActivityDashboardSummary activity,
            ReconciliationDashboardSummary reconciliation
    ) {
    }

    public record CurrencyBalanceSummary(
            CurrencyCode currency,
            BigDecimal amount
    ) {
    }

    public record LoanDashboardSummary(
            long totalLoans,
            long activeLoans,
            long partiallyPaidLoans,
            long paidLoans,
            long cancelledLoans,
            BigDecimal totalPrincipal,
            BigDecimal totalPayable,
            BigDecimal totalOutstanding
    ) {
    }

    public record ClientLoanDashboardSummary(
            long totalLoans,
            long activeLoans,
            long partiallyPaidLoans,
            long paidLoans,
            long cancelledLoans,
            BigDecimal totalPrincipal,
            BigDecimal totalReceivable,
            BigDecimal totalOutstanding
    ) {
    }

    public record ActivityDashboardSummary(
            BigDecimal floatIn,
            BigDecimal floatOut,
            BigDecimal bankIn,
            BigDecimal bankOut,
            BigDecimal floatToBankDeposits,
            BigDecimal bankToFloatWithdrawals,
            BigDecimal expensesFromFloat,
            BigDecimal expensesFromBank,
            BigDecimal investorLoansReceived,
            BigDecimal investorLoanRepayments,
            BigDecimal clientLoansIssued,
            BigDecimal clientLoanRepayments,
            BigDecimal reconciliationAdjustments
    ) {
    }

    public record ReconciliationDashboardSummary(
            long totalReconciliations,
            long draftCount,
            long balancedCount,
            long partiallyReconciledCount,
            long unresolvedCount,
            long cancelledCount,
            BigDecimal totalDifference,
            BigDecimal totalShortAmount,
            BigDecimal totalExcessAmount,
            BigDecimal totalReconciledAmount,
            BigDecimal totalUnreconciledAmount,
            List<ReconciliationStatusCount> statusCounts,
            List<UnresolvedReconciliationInsight> unresolvedItems,
            List<BankReconciliationHealth> bankAccountHealth,
            List<RecentReconciliation> recentReconciliations
    ) {
    }

    public record ReconciliationStatusCount(
            ReconciliationStatus status,
            long count
    ) {
    }

    public record UnresolvedReconciliationInsight(
            Long reconciliationId,
            Long bankAccountId,
            String bankName,
            String accountName,
            String accountNumber,
            Long branchId,
            String branchName,
            CurrencyCode currency,
            LocalDate reconciliationDate,
            BigDecimal systemBalanceBefore,
            BigDecimal statementBalance,
            BigDecimal difference,
            ReconciliationDifferenceType differenceType,
            BigDecimal reconciledAmount,
            BigDecimal unreconciledAmount,
            String reference,
            String reason,
            String actionedBy
    ) {
    }

    public record BankReconciliationHealth(
            Long bankAccountId,
            String bankName,
            String accountName,
            String accountNumber,
            CurrencyCode currency,
            BigDecimal currentSystemBalance,
            LocalDate lastReconciliationDate,
            ReconciliationStatus lastStatus,
            ReconciliationDifferenceType lastDifferenceType,
            BigDecimal lastDifference,
            BigDecimal lastUnreconciledAmount,
            long daysSinceLastReconciliation,
            String healthStatus
    ) {
    }

    public record RecentReconciliation(
            Long reconciliationId,
            Long bankAccountId,
            String bankName,
            String accountName,
            String accountNumber,
            Long branchId,
            String branchName,
            CurrencyCode currency,
            LocalDate reconciliationDate,
            BigDecimal systemBalanceBefore,
            BigDecimal statementBalance,
            BigDecimal difference,
            ReconciliationDifferenceType differenceType,
            BigDecimal reconciledAmount,
            BigDecimal unreconciledAmount,
            BigDecimal systemBalanceAfter,
            ReconciliationStatus status,
            String reference,
            String reason,
            String actionedBy
    ) {
    }
}