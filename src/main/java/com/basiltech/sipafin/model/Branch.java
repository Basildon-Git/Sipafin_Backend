package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "branches",
        indexes = {
                @Index(name = "idx_branches_code", columnList = "code"),
                @Index(name = "idx_branches_active", columnList = "active")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_branches_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_branches_name", columnNames = "name")
        }
)
@Getter
@Setter
public class Branch extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String location;

    @Column(nullable = false)
    private boolean active = true;
}