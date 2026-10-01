package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "client_loan_accounts",
        indexes = {
                @Index(name = "idx_client_loan_accounts_branch_id", columnList = "branch_id"),
                @Index(name = "idx_client_loan_accounts_client_phone", columnList = "client_phone"),
                @Index(name = "idx_client_loan_accounts_status", columnList = "status"),
                @Index(name = "idx_client_loan_accounts_currency", columnList = "currency")
        }
)
@Getter
@Setter
public class ClientLoanAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @Column(name = "client_phone", nullable = false, length = 50)
    private String clientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CurrencyCode currency;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;

    @Column(name = "interest_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(name = "interest_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal interestAmount = BigDecimal.ZERO;

    @Column(name = "total_receivable", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalReceivable = BigDecimal.ZERO;

    @Column(name = "outstanding_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "funding_source", nullable = false, length = 50)
    private MoneySourceType fundingSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_bank_account_id")
    private BankAccount fundingBankAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClientLoanStatus status = ClientLoanStatus.ACTIVE;

    @Column(name = "date_issued", nullable = false)
    private LocalDate dateIssued;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 255)
    private String description;
}