package ru.yandex.practicum.bank.cash.domain;

public enum CashOperationStage {
    NEW,
    REJECTED_INSUFFICIENT_FUNDS,
    NOTIFICATION_PENDING,
    COMPLETED,
}
