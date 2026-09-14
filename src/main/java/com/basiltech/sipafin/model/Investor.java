package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "investors",
        indexes = {
                @Index(name = "idx_investors_full_name", columnList = "full_name"),
                @Index(name = "idx_investors_phone_number", columnList = "phone_number"),
                @Index(name = "idx_investors_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_investors_phone_number", columnNames = "phone_number")
        }
)
@Getter
@Setter
public class Investor extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Column(name = "national_id", length = 100)
    private String nationalId;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private boolean active = true;
}