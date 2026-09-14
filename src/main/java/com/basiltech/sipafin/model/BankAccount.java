package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "bank_accounts",
        indexes = {
                @Index(name = "idx_bank_accounts_bank_id", columnList = "bank_id"),
                @Index(name = "idx_bank_accounts_account_number", columnList = "account_number"),
                @Index(name = "idx_bank_accounts_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_bank_accounts_account_number", columnNames = "account_number")
        }
)
@Getter
@Setter
public class BankAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = false)
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "account_name", nullable = false, length = 150)
    private String accountName;

    @Column(name = "account_number", nullable = false, length = 100)
    private String accountNumber;

    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}