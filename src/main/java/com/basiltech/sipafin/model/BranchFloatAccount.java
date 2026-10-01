package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "branch_float_accounts",
        indexes = {
                @Index(name = "idx_branch_float_accounts_branch_id", columnList = "branch_id"),
                @Index(name = "idx_branch_float_accounts_currency", columnList = "currency"),
                @Index(name = "idx_branch_float_accounts_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_branch_float_accounts_branch_currency",
                        columnNames = {"branch_id", "currency"}
                )
        }
)
@Getter
@Setter
public class BranchFloatAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;
}