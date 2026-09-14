package com.basiltech.sipafin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "user_accounts",
        indexes = {
                @Index(name = "idx_user_accounts_username", columnList = "username"),
                @Index(name = "idx_user_accounts_role", columnList = "role")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_accounts_username", columnNames = "username")
        }
)
@Getter
@Setter
public class UserAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = true)
    private Branch branch;

    @Column(nullable = false)
    private boolean enabled = true;
}