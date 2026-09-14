package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "expenses",
        indexes = {
                @Index(name = "idx_expenses_branch_id", columnList = "branch_id"),
                @Index(name = "idx_expenses_bank_account_id", columnList = "bank_account_id"),
                @Index(name = "idx_expenses_category", columnList = "category"),
                @Index(name = "idx_expenses_expense_date", columnList = "expense_date"),
                @Index(name = "idx_expenses_payment_source", columnList = "payment_source"),
                @Index(name = "idx_expenses_status", columnList = "status"),
                @Index(name = "idx_expenses_reference", columnList = "reference")
        }
)
@Getter
@Setter
public class Expense extends Auditable {

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
    @Column(nullable = false, length = 50)
    private ExpenseCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_source", nullable = false, length = 50)
    private PaymentSource paymentSource = PaymentSource.PETTY_CASH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExpenseStatus status = ExpenseStatus.POSTED;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}