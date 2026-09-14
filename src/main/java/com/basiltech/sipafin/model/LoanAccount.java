package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "loan_accounts",
        indexes = {
                @Index(name = "idx_loan_accounts_investor_id", columnList = "investor_id"),
                @Index(name = "idx_loan_accounts_branch_id", columnList = "branch_id"),
                @Index(name = "idx_loan_accounts_status", columnList = "status"),
                @Index(name = "idx_loan_accounts_date_received", columnList = "date_received")
        }
)
@Getter
@Setter
public class LoanAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private Investor investor;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount = BigDecimal.ZERO;

    @Column(name = "outstanding_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingBalance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(name = "date_received", nullable = false)
    private LocalDate dateReceived;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 255)
    private String description;
}