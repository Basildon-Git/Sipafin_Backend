package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "daily_branch_summaries",
        indexes = {
                @Index(name = "idx_daily_branch_summaries_branch_id", columnList = "branch_id"),
                @Index(name = "idx_daily_branch_summaries_summary_date", columnList = "summary_date"),
                @Index(name = "idx_daily_branch_summaries_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_branch_summaries_branch_date",
                        columnNames = {"branch_id", "summary_date"}
                )
        }
)
@Getter
@Setter
public class DailyBranchSummary extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "loans_received", nullable = false, precision = 19, scale = 2)
    private BigDecimal loansReceived = BigDecimal.ZERO;

    @Column(name = "loan_repayments", nullable = false, precision = 19, scale = 2)
    private BigDecimal loanRepayments = BigDecimal.ZERO;

    @Column(name = "commissions_received", nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionsReceived = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal expenses = BigDecimal.ZERO;

    @Column(name = "cash_disbursed", nullable = false, precision = 19, scale = 2)
    private BigDecimal cashDisbursed = BigDecimal.ZERO;

    @Column(name = "bank_deposits", nullable = false, precision = 19, scale = 2)
    private BigDecimal bankDeposits = BigDecimal.ZERO;

    @Column(name = "bank_withdrawals", nullable = false, precision = 19, scale = 2)
    private BigDecimal bankWithdrawals = BigDecimal.ZERO;

    @Column(name = "adjustments", nullable = false, precision = 19, scale = 2)
    private BigDecimal adjustments = BigDecimal.ZERO;

    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal closingBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DailySummaryStatus status = DailySummaryStatus.OPEN;

    @Column(name = "closed_by", length = 150)
    private String closedBy;

    @Column(name = "closed_at")
    private Instant closedAt;
}