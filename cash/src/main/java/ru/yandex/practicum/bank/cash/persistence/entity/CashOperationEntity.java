package ru.yandex.practicum.bank.cash.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;
import ru.yandex.practicum.bank.cash.domain.CashOperationStage;

import java.time.Instant;

@Entity
@Table(name = "cash_operations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CashOperationEntity {
    @Id
    @Column(name = "operation_id", nullable = false)
    private String operationId;

    @Column(name = "login", nullable = false)
    private String login;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BalanceOperationType type;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private CashOperationStage stage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CashOperationEntity(
        String operationId,
        String login,
        BalanceOperationType type,
        int amount,
        CashOperationStage stage
    ) {
        this.operationId = operationId;
        this.login = login;
        this.type = type;
        this.amount = amount;
        this.stage = stage;
    }
}
