package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "loan_transactions",
        indexes = {
                @Index(name = "idx_loan_transactions_loan_account_id", columnList = "loan_account_id"),
                @Index(name = "idx_loan_transactions_branch_id", columnList = "branch_id"),
                @Index(name = "idx_loan_transactions_type", columnList = "transaction_type"),
                @Index(name = "idx_loan_transactions_date", columnList = "transaction_date"),
                @Index(name = "idx_loan_transactions_reference", columnList = "reference")
        }
)
@Getter
@Setter
public class LoanTransaction extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private LoanTransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}