package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "branch_cash_disbursements",
        indexes = {
                @Index(name = "idx_branch_cash_disbursements_branch_id", columnList = "branch_id"),
                @Index(name = "idx_branch_cash_disbursements_disbursement_date", columnList = "disbursement_date"),
                @Index(name = "idx_branch_cash_disbursements_status", columnList = "status"),
                @Index(name = "idx_branch_cash_disbursements_reference", columnList = "reference")
        }
)
@Getter
@Setter
public class BranchCashDisbursement extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "disbursement_date", nullable = false)
    private LocalDate disbursementDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BranchCashDisbursementStatus status = BranchCashDisbursementStatus.POSTED;

    @Column(length = 100)
    private String reference;

    @Column(length = 255)
    private String description;
}