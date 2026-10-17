package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "revenues",
        indexes = {
                @Index(name = "idx_revenues_branch_id", columnList = "branch_id"),
                @Index(name = "idx_revenues_bank_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_revenues_currency", columnList = "currency"),
                @Index(name = "idx_revenues_category", columnList = "category"),
                @Index(name = "idx_revenues_revenue_date", columnList = "revenue_date"),
                @Index(name = "idx_revenues_source", columnList = "source"),
                @Index(name = "idx_revenues_status", columnList = "status")
        }
)
@Getter
@Setter
public class Revenue extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RevenueCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MoneySourceType source;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "revenue_date", nullable = false)
    private LocalDate revenueDate;

    @Column(name = "transaction_group_id", nullable = false, length = 100)
    private String transactionGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RevenueStatus status = RevenueStatus.POSTED;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}