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
                @Index(name = "idx_commissions_bank_id", columnList = "bank_id"),
                @Index(name = "idx_commissions_bank_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_commissions_date", columnList = "commission_date"),
                @Index(name = "idx_commissions_status", columnList = "status"),
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
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "commission_date", nullable = false)
    private LocalDate commissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CommissionDestination destination = CommissionDestination.BANK_ACCOUNT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CommissionStatus status = CommissionStatus.RECEIVED;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}