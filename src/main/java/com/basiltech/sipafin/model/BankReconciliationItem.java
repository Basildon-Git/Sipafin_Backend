package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "bank_reconciliation_items",
        indexes = {
                @Index(name = "idx_bank_reconciliation_items_reconciliation_id", columnList = "reconciliation_id"),
                @Index(name = "idx_bank_reconciliation_items_category", columnList = "category"),
                @Index(name = "idx_bank_reconciliation_items_effect", columnList = "effect"),
                @Index(name = "idx_bank_reconciliation_items_transaction_date", columnList = "transaction_date"),
                @Index(name = "idx_bank_reconciliation_items_bank_transaction_id", columnList = "bank_transaction_id")
        }
)
@Getter
@Setter
public class BankReconciliationItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reconciliation_id", nullable = false)
    private BankReconciliation reconciliation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReconciliationItemCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReconciliationItemEffect effect;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_transaction_id")
    private BankTransaction bankTransaction;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}