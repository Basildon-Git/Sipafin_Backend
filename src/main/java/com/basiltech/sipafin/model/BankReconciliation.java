package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "bank_reconciliations",
        indexes = {
                @Index(name = "idx_bank_reconciliations_bank_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_bank_reconciliations_branch_id", columnList = "branch_id"),
                @Index(name = "idx_bank_reconciliations_currency", columnList = "currency"),
                @Index(name = "idx_bank_reconciliations_date", columnList = "reconciliation_date"),
                @Index(name = "idx_bank_reconciliations_status", columnList = "status"),
                @Index(name = "idx_bank_reconciliations_group_id", columnList = "transaction_group_id")
        }
)
@Getter
@Setter
public class BankReconciliation extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "system_balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal systemBalanceBefore;

    @Column(name = "statement_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal statementBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal difference;

    @Column(name = "adjustment_applied", nullable = false)
    private boolean adjustmentApplied;

    @Column(name = "system_balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal systemBalanceAfter;

    @Column(name = "transaction_group_id", length = 100)
    private String transactionGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReconciliationStatus status;

    @Column(length = 100)
    private String reference;

    @Column(nullable = false, length = 255)
    private String reason;
}