package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "banks",
        indexes = {
                @Index(name = "idx_banks_code", columnList = "code"),
                @Index(name = "idx_banks_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_banks_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_banks_name", columnNames = "name")
        }
)
@Getter
@Setter
public class Bank extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private boolean active = true;
}