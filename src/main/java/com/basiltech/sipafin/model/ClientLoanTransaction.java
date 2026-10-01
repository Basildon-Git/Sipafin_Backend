package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "client_loan_transactions",
        indexes = {
                @Index(name = "idx_client_loan_transactions_client_loan_id", columnList = "client_loan_account_id"),
                @Index(name = "idx_client_loan_transactions_branch_id", columnList = "branch_id"),
                @Index(name = "idx_client_loan_transactions_currency", columnList = "currency"),
                @Index(name = "idx_client_loan_transactions_date", columnList = "transaction_date"),
                @Index(name = "idx_client_loan_transactions_type", columnList = "transaction_type")
        }
)
@Getter
@Setter
public class ClientLoanTransaction extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "client_loan_account_id", nullable = false)
    private ClientLoanAccount clientLoanAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private ClientLoanTransactionType transactionType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "transaction_group_id", nullable = false, length = 100)
    private String transactionGroupId;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}