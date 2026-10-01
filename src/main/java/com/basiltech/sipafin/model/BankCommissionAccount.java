package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "bank_commission_accounts",
        indexes = {
                @Index(name = "idx_bank_commission_accounts_bank_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_bank_commission_accounts_currency", columnList = "currency"),
                @Index(name = "idx_bank_commission_accounts_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bank_commission_accounts_bank_account",
                        columnNames = "bank_account_id"
                )
        }
)
@Getter
@Setter
public class BankCommissionAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Column(name = "commission_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal commissionRate = BigDecimal.ZERO;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}
