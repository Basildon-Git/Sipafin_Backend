package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "bank_transactions",
        indexes = {
                @Index(name = "idx_bank_transactions_bank_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_bank_transactions_branch_id", columnList = "branch_id"),
                @Index(name = "idx_bank_transactions_currency", columnList = "currency"),
                @Index(name = "idx_bank_transactions_type", columnList = "transaction_type"),
                @Index(name = "idx_bank_transactions_direction", columnList = "direction"),
                @Index(name = "idx_bank_transactions_date", columnList = "transaction_date"),
                @Index(name = "idx_bank_transactions_group_id", columnList = "transaction_group_id"),
                @Index(name = "idx_bank_transactions_status", columnList = "status")
        }
)
@Getter
@Setter
public class BankTransaction extends Auditable {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private BankLedgerTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MoneyDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "transaction_group_id", nullable = false, length = 100)
    private String transactionGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionStatus status = TransactionStatus.POSTED;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}