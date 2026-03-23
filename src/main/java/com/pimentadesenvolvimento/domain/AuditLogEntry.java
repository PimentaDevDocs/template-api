package com.pimentadesenvolvimento.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Immutable audit log entry. Unlike regular entities, audit logs are not soft-deleted.
 * Logs represent historical facts that must not be altered or removed.
 */
@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Table(name = "audit_log")
@EntityListeners(AuditingEntityListener.class)
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String action;
    @Column(nullable = true)
    private String principal;
    @Column(length = 2000)
    private String details;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLogEntry(String action, String principal, String details) {
        this.action = action;
        this.principal = principal;
        this.details = details;
    }
}
