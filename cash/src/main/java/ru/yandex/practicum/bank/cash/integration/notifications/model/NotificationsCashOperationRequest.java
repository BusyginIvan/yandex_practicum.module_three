package ru.yandex.practicum.bank.cash.integration.notifications.model;

import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;

public record NotificationsCashOperationRequest(
    String login,
    BalanceOperationType type,
    int amount
) { }
