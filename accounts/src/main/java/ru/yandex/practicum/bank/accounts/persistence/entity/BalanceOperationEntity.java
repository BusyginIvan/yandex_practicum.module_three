package ru.yandex.practicum.bank.accounts.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationStatus;
import ru.yandex.practicum.bank.accounts.domain.BalanceOperationType;

@Entity
@Table(name = "balance_operations")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BalanceOperationEntity {
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BalanceOperationStatus status;
}
