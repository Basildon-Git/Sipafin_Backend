package com.basiltech.sipafin.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public abstract class Auditable {

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "actioned_by", nullable = false)
    private String actionedBy;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();

        if (this.createdAt == null) {
            this.createdAt = now;
        }

        this.updatedAt = now;

        if (this.actionedBy == null || this.actionedBy.isBlank()) {
            this.actionedBy = "system";
        }
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();

        if (this.actionedBy == null || this.actionedBy.isBlank()) {
            this.actionedBy = "system";
        }
    }
}