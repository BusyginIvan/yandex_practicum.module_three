package ru.yandex.practicum.bank.cash.api.model;

import ru.yandex.practicum.bank.cash.domain.BalanceOperationType;

public record CashOperationRequest(
    BalanceOperationType type,
    int amount
) { }
