package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "commissions",
        indexes = {
                @Index(name = "idx_commissions_branch_id", columnList = "branch_id"),
                @Index(name = "idx_commissions_bank_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_commissions_commission_account_id", columnList = "commission_account_id"),
                @Index(name = "idx_commissions_currency", columnList = "currency"),
                @Index(name = "idx_commissions_date", columnList = "commission_date"),
                @Index(name = "idx_commissions_status", columnList = "status"),
                @Index(name = "idx_commissions_group_id", columnList = "transaction_group_id"),
                @Index(name = "idx_commissions_reference", columnList = "reference")
        }
)
@Getter
@Setter
public class Commission extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "commission_account_id", nullable = false)
    private BankCommissionAccount commissionAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Column(name = "base_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "commission_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "commission_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Column(name = "commission_date", nullable = false)
    private LocalDate commissionDate;

    @Column(name = "transaction_group_id", nullable = false, length = 100)
    private String transactionGroupId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CommissionStatus status = CommissionStatus.RECEIVED;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}