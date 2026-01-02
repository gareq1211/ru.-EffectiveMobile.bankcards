package ru.effectivemobile.bankcards.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_audit")
@Getter
@Setter
public class CardAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "old_balance")
    private BigDecimal oldBalance;

    @Column(name = "new_balance")
    private BigDecimal newBalance;

    @Enumerated(EnumType.STRING)
    private CardStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private CardStatus newStatus;

    @Column(name = "performed_by", nullable = false)
    private String performedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

