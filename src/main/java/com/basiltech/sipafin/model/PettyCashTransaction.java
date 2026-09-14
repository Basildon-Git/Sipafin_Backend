package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "petty_cash_transactions",
        indexes = {
                @Index(name = "idx_petty_cash_transactions_branch_id", columnList = "branch_id"),
                @Index(name = "idx_petty_cash_transactions_type", columnList = "transaction_type"),
                @Index(name = "idx_petty_cash_transactions_direction", columnList = "direction"),
                @Index(name = "idx_petty_cash_transactions_date", columnList = "transaction_date"),
                @Index(name = "idx_petty_cash_transactions_reference", columnList = "reference")
        }
)
@Getter
@Setter
public class PettyCashTransaction extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private PettyCashTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CashDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "transaction_group_id", length = 100)
    private String transactionGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionStatus status = TransactionStatus.POSTED;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}